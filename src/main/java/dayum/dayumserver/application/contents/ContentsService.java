package dayum.dayumserver.application.contents;

import dayum.dayumserver.application.common.exception.AppException;
import dayum.dayumserver.application.common.exception.AuthExceptionCode;
import dayum.dayumserver.application.common.request.LoginMember;
import dayum.dayumserver.application.common.response.PageResponse;
import dayum.dayumserver.application.contents.dto.internal.ExtractedIngredientData;
import dayum.dayumserver.application.contents.dto.request.ContentsRecommendRequest;
import dayum.dayumserver.application.contents.dto.request.ContentsUploadRequest;
import dayum.dayumserver.application.contents.dto.response.ContentsAnalyzeResponse;
import dayum.dayumserver.application.contents.dto.response.ContentsDetailResponse;
import dayum.dayumserver.application.contents.dto.response.ContentsResponse;
import dayum.dayumserver.application.ingredient.IngredientService;
import dayum.dayumserver.client.cv.FrameExtractorService;
import dayum.dayumserver.client.s3.S3ClientService;
import dayum.dayumserver.common.helper.FileHelper;
import dayum.dayumserver.domain.contents.Contents;
import dayum.dayumserver.domain.contents.ContentsIngredient;
import dayum.dayumserver.domain.contents.ContentsIngredientRepository;
import dayum.dayumserver.domain.contents.ContentsRepository;
import dayum.dayumserver.domain.ingredient.Ingredient;
import dayum.dayumserver.domain.member.MemberRepository;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentsService {

  private final ContentsRepository contentsRepository;
  private final MemberRepository memberRepository;
  private final ContentAnalysisService contentAnalysisService;
  private final FrameExtractorService frameExtractorService;
  private final S3ClientService s3ClientService;
  private final IngredientService ingredientService;
  private final ContentsIngredientRepository contentsIngredientRepository;

  public PageResponse<ContentsResponse> retrieveNextPage(Long memberId, long cursorId, int size) {
    var contentsList =
        switch (memberId) {
          case null -> contentsRepository.fetchNextPage(cursorId, size + 1);
          default -> contentsRepository.fetchNextPageByMember(memberId, cursorId, size + 1);
        };

    if (contentsList.size() <= size) {
      var items = contentsList.stream().map(ContentsResponse::from).toList();
      return new PageResponse<>(items, new PageResponse.PageInfo("", true));
    }
    var items = contentsList.subList(0, size).stream().map(ContentsResponse::from).toList();
    var nextItem = contentsList.getLast();
    return new PageResponse<>(
        items, new PageResponse.PageInfo(String.valueOf(nextItem.id()), false));
  }

  public ContentsDetailResponse retrieve(long id) {
    var contents = contentsRepository.fetchBy(id);
    return ContentsDetailResponse.from(contents);
  }

  public List<ContentsDetailResponse> recommend(ContentsRecommendRequest request) {
    // TODO(chanjun.park):  For the MVP, recommendations are based only on the presence of
    //  ingredients. We need to improve it to take ingredient quantities into account.

    log.info(">> recommend 호출 | 요청된 재료: {}", request.ingredients());

    var ingredientNames =
        request.ingredients().stream()
            .map(ContentsRecommendRequest.IngredientRequest::name)
            .toList();
    log.info("추출된 재료 이름 목록: {}", ingredientNames);

    var ingredients = ingredientService.findIngredientsByNames(ingredientNames);

    var result =
        contentsRepository.fetchMakeableContents(ingredients, request.maxCount()).stream()
            .map(ContentsDetailResponse::from)
            .toList();

    return result;
  }

  public ContentsAnalyzeResponse analyze(String contentsUrl, Long memberId) {
    String thumbnailUrl;
    List<ExtractedIngredientData> extractedIngredients;

    Path workingDir = FileHelper.createWorkingDirectory();
    try {
      File contentsFile = s3ClientService.downloadFile(contentsUrl, workingDir);
      File thumbnail = frameExtractorService.extractThumbnail(contentsFile, workingDir);
      thumbnailUrl = s3ClientService.uploadFile("contents/thumbnails", thumbnail, workingDir);
      extractedIngredients =
          contentAnalysisService.analyzeIngredients(contentsUrl, contentsFile, workingDir);
    } finally {
      FileHelper.deleteWorkingDirectory(workingDir);
    }

    var contents =
        Contents.createDraft(memberRepository.fetchBy(memberId), contentsUrl, thumbnailUrl);
    var savedContents = contentsRepository.save(contents);

    List<String> ingredientNames =
        extractedIngredients.stream().map(ExtractedIngredientData::name).toList();
    List<Ingredient> ingredients = ingredientService.findIngredientsByNames(ingredientNames);
    return ContentsAnalyzeResponse.from(savedContents.id(), ingredients);
  }

  public String addIngredients(Long contentsId, ContentsUploadRequest contentsUploadRequest) {
    var contents = contentsRepository.fetchBy(contentsId);

    List<Long> ingredientIds =
        contentsUploadRequest.ingredients().stream()
            .map(ContentsUploadRequest.IngredientDto::id)
            .toList();

    List<Ingredient> ingredients = ingredientService.findAllByIds(ingredientIds);
    if (ingredients.size() != ingredientIds.size()) {
      throw new IllegalArgumentException("일부 재료를 찾을 수 없습니다");
    }

    Map<Long, Long> quantityMap =
        contentsUploadRequest.ingredients().stream()
            .collect(
                Collectors.toMap(
                    ContentsUploadRequest.IngredientDto::id,
                    ContentsUploadRequest.IngredientDto::quantity));

    List<ContentsIngredient> contentsIngredients =
        ingredients.stream()
            .map(
                ingredient ->
                    ContentsIngredient.from(contents, ingredient, quantityMap.get(ingredient.id())))
            .toList();

    contentsIngredientRepository.saveAll(contentsIngredients);

    return contentsRepository.save(contents.publish()).url();
  }

  public void delete(long contentsId, LoginMember member) {
    var contents = contentsRepository.fetchBy(contentsId);
    if (contents.isOwner(member.id())) {
      throw new AppException(AuthExceptionCode.ACCESS_DENIED);
    }
    contentsIngredientRepository.deleteAll(contents.ingredients());
    contentsRepository.delete(contents);
  }
}
