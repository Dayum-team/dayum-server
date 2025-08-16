package dayum.dayumserver.application.contents.dto.request;

import java.util.List;

public record ContentsRecommendRequest(List<IngredientRequest> ingredients, int maxCount) {

  public record IngredientRequest(String name, String quantity) {}
}
