package dayum.dayumserver.domain.contents;

import dayum.dayumserver.domain.ingredient.Ingredient;
import lombok.Builder;

@Builder
public record ContentsIngredient(Long id, Contents contents, Ingredient ingredient, long quantity) {

  public static ContentsIngredient from(Contents contents, Ingredient ingredient, long quantity) {
    return ContentsIngredient.builder()
        .contents(contents)
        .ingredient(ingredient)
        .quantity(quantity)
        .build();
  }
}
