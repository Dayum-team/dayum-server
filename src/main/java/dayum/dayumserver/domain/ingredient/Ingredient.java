package dayum.dayumserver.domain.ingredient;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Ingredient {

  private Long id;
  private final String name;
  private final String standardQuantity;
  private final Double calories;
  private final Double carbohydrates;
  private final Double proteins;
  private final Double fats;
  private final Double sugars;
  private final Double sodium;

  @Builder
  public Ingredient(
      String name,
      String standardQuantity,
      Double calories,
      Double carbohydrates,
      Double proteins,
      Double fats,
      Double sugars,
      Double sodium) {
    this.name = name;
    this.standardQuantity = standardQuantity;
    this.calories = calories;
    this.carbohydrates = carbohydrates;
    this.proteins = proteins;
    this.fats = fats;
    this.sugars = sugars;
    this.sodium = sodium;
  }
}
