package dayum.dayumserver.domain.contents;

import dayum.dayumserver.domain.member.Member;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record Contents(
    Long id,
    Member member,
    String thumbnailUrl,
    String url,
    List<ContentsIngredient> ingredients,
    ContentStatus status,
    LocalDateTime createdAt) {

  public static Contents createDraft(Member member, String url, String thumbnailUrl) {
    return Contents.builder().member(member).url(url).status(ContentStatus.PENDING).build();
  }

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

  public Contents publish() {
    return Contents.builder()
        .id(id)
        .member(member)
        .thumbnailUrl(thumbnailUrl)
        .url(url)
        .ingredients(ingredients)
        .status(ContentStatus.PUBLISHED)
        .createdAt(createdAt)
        .build();
  }
}
