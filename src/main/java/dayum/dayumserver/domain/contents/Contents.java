package dayum.dayumserver.domain.contents;

import dayum.dayumserver.domain.member.Member;
import java.time.LocalDateTime;
import java.util.List;

public record Contents(
    Long id,
    Member member,
    String thumbnailUrl,
    String url,
    List<ContentsIngredient> ingredients,
    LocalDateTime createdAt) {

  public double calculateCalories() {
    return ingredients.stream().mapToDouble(it -> it.ingredient().calories() * it.quantity()).sum();
  }

  public double calculateCarbohydrates() {
    return ingredients.stream()
        .mapToDouble(it -> it.ingredient().carbohydrates() * it.quantity())
        .sum();
  }

  public double calculateProteins() {
    return ingredients.stream().mapToDouble(it -> it.ingredient().proteins() * it.quantity()).sum();
  }

  public double calculateFats() {
    return ingredients.stream().mapToDouble(it -> it.ingredient().fats() * it.quantity()).sum();
  }
}
