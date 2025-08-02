package dayum.dayumserver.domain.ingredient;


public record Ingredient(
    Long id,
    String name,
    String standardQuantity,
    Double calories,
    Double carbohydrates,
    Double proteins,
    Double fats,
    Double sugars,
    Double sodium) {}
