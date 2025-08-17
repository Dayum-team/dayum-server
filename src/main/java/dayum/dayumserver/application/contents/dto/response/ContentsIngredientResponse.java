package dayum.dayumserver.application.contents.dto.response;

import dayum.dayumserver.domain.contents.ContentsIngredient;

public record ContentsIngredientResponse(
    long id,
    String name,
    String standardQuantity,
    long quantity,
    double calories,
    double carbohydrates,
    double proteins,
    double fats,
    double sugars,
    double sodium) {

  public static ContentsIngredientResponse from(ContentsIngredient contentsIngredient) {
    return new ContentsIngredientResponse(
        contentsIngredient.id(),
        contentsIngredient.ingredient().name(),
        contentsIngredient.ingredient().standardQuantity(),
        contentsIngredient.quantity(),
        contentsIngredient.ingredient().calories() * contentsIngredient.quantity(),
        contentsIngredient.ingredient().carbohydrates() * contentsIngredient.quantity(),
        contentsIngredient.ingredient().proteins() * contentsIngredient.quantity(),
        contentsIngredient.ingredient().fats() * contentsIngredient.quantity(),
        contentsIngredient.ingredient().sugars() * contentsIngredient.quantity(),
        contentsIngredient.ingredient().sodium() * contentsIngredient.quantity());
  }
}
