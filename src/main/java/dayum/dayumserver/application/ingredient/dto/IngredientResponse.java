package dayum.dayumserver.application.ingredient.dto;

import dayum.dayumserver.domain.ingredient.Ingredient;

public record IngredientResponse(Long id, String name) {

  public static IngredientResponse from(Ingredient ingredient) {
    return new IngredientResponse(ingredient.id(), ingredient.name());
  }
}
