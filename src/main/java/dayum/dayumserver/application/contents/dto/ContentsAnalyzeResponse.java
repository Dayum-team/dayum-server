package dayum.dayumserver.application.contents.dto;

import java.util.List;
import java.util.stream.Collectors;

import dayum.dayumserver.domain.ingredient.Ingredient;

public record ContentsAnalyzeResponse(Long contentsId, List<IngredientInfo> ingredients) {

  public record IngredientInfo(
      Long ingredientId, String ingredientName, Double calories, String standardQuantity) {

    public static IngredientInfo from(Ingredient ingredient) {
      return new IngredientInfo(
          ingredient.id(), ingredient.name(), ingredient.calories(), ingredient.standardQuantity());
    }
  }

  public static ContentsAnalyzeResponse from(Long contentsId, List<Ingredient> ingredients) {
    List<IngredientInfo> ingredientInfos =
        ingredients.stream().map(IngredientInfo::from).collect(Collectors.toList());

    return new ContentsAnalyzeResponse(contentsId, ingredientInfos);
  }
}
