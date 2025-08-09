package dayum.dayumserver.application.ingredient.dto;

import dayum.dayumserver.domain.ingredient.Ingredient;

public record IngredientResponse(
    Long id,
    String name,
    String standardQuantity,
    Double calories,
    Double carbohydrates,
    Double proteins,
    Double fats,
    Double sugars,
    Double sodium) {

  public static IngredientResponse from(Ingredient ingredient) {
    return new IngredientResponse(
        ingredient.id(),
        ingredient.name(),
        ingredient.standardQuantity(),
        ingredient.calories(),
        ingredient.carbohydrates(),
        ingredient.proteins(),
        ingredient.fats(),
        ingredient.sugars(),
        ingredient.sodium());
  }
}
