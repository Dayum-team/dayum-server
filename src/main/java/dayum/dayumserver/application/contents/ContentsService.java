package dayum.dayumserver.application.contents;

import dayum.dayumserver.application.common.response.PageResponse;
import dayum.dayumserver.application.contents.dto.ContentsAnalyzeResponse;
import dayum.dayumserver.application.contents.dto.ContentsDetailResponse;
import dayum.dayumserver.application.contents.dto.ContentsResponse;
import dayum.dayumserver.application.contents.dto.internal.ExtractedIngredientData;
import dayum.dayumserver.application.ingredient.IngredientService;
import dayum.dayumserver.domain.contents.Contents;
import dayum.dayumserver.domain.contents.ContentsRepository;
import dayum.dayumserver.domain.ingredient.Ingredient;
import dayum.dayumserver.domain.member.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentsService {

  private final ContentsRepository contentsRepository;
  private final MemberRepository memberRepository;
  private final ContentAnalysisService contentAnalysisService;
  private final IngredientService ingredientService;

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
    Long contentsId = contentsRepository.save(contents).id();

    List<ExtractedIngredientData> analysisResult =
        contentAnalysisService.analyzeIngredients(contentsUrl);

    List<String> ingredientNames = analysisResult.stream()
        .map(ExtractedIngredientData::name)
        .toList();

    List<Ingredient> ingredients = ingredientService.findIngredientsByNames(ingredientNames);

    return ContentsAnalyzeResponse.from(contentsId, ingredients);
  }
}
