package dayum.dayumserver.domain.contents;

import dayum.dayumserver.domain.ingredient.Ingredient;
import dayum.dayumserver.domain.member.Member;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Contents {

  private Long id;
  private final Member member;
  private final String url;
  private final List<Ingredient> ingredients;

  @Builder
  public Contents(Member member, String url, List<Ingredient> ingredients) {
    this.member = member;
    this.url = url;
    this.ingredients = ingredients;
  }
}
