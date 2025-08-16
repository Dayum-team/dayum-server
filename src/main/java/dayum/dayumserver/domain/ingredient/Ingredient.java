package dayum.dayumserver.domain.ingredient;

public record Ingredient(
    Long id,
    String name,
    String standardQuantity,
    double calories,
    double carbohydrates,
    double proteins,
    double fats,
    double sugars,
    double sodium) {}
