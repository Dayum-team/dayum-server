package dayum.dayumserver.domain.member;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Member {

  private Long id;
  private String nickname;

  // TODO: After finalizing the ERD, add the remaining column details.

  @Builder
  public Member(String nickname) {
    this.nickname = nickname;
  }
}
