package dayum.dayumserver.domain.contents;

import dayum.dayumserver.domain.ingredient.Ingredient;
import dayum.dayumserver.domain.member.Member;
import java.time.LocalDateTime;
import java.util.List;

public record Contents(
    Long id,
    Member member,
    String thumbnailUrl,
    String url,
    List<Ingredient> ingredients,
    ContentStatus status,
    LocalDateTime createdAt) {

  public static Contents createDraft(Member member, String url) {
    return new Contents(null, member, null, url, null, ContentStatus.PENDING, null);
  }
}
