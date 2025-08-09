package dayum.dayumserver.application.contents;

import dayum.dayumserver.application.common.response.PageResponse;
import dayum.dayumserver.application.contents.dto.request.ContentsUploadRequest;
import dayum.dayumserver.application.contents.dto.response.ContentsAnalyzeResponse;
import dayum.dayumserver.application.contents.dto.response.ContentsDetailResponse;
import dayum.dayumserver.application.contents.dto.response.ContentsResponse;
import dayum.dayumserver.application.contents.dto.internal.ExtractedIngredientData;
import dayum.dayumserver.application.ingredient.IngredientService;
import dayum.dayumserver.domain.contents.Contents;
import dayum.dayumserver.domain.contents.ContentsIngredient;
import dayum.dayumserver.domain.contents.ContentsIngredientRepository;
import dayum.dayumserver.domain.contents.ContentsRepository;
import dayum.dayumserver.domain.ingredient.Ingredient;
import dayum.dayumserver.domain.member.MemberRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentsService {

  private final ContentsRepository contentsRepository;
  private final MemberRepository memberRepository;
  private final ContentAnalysisService contentAnalysisService;
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

  public ContentsAnalyzeResponse extractIngredientsFromContent(String contentsUrl, Long memberId) {

    var contents = Contents.createDraft(memberRepository.fetchBy(memberId), contentsUrl);
    contentsRepository.save(contents);

    List<ExtractedIngredientData> analysisResult =
        contentAnalysisService.analyzeIngredients(contentsUrl);

    // TODO 추출된 재료와 DB 데이터 매핑후 반환

    return new ContentsAnalyzeResponse();
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
}
