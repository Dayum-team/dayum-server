package dayum.dayumserver.domain.member;

import java.time.LocalDateTime;
import lombok.Builder;

// TODO: After finalizing the ERD, add the remaining column details.
@Builder
public record Member(
    Long id,
    String email,
    String name,
    String nickname,
    String profileImage,
    Oauth2Provider oauth2Provider,
    String bio,
    boolean deleted,
    LocalDateTime deletedAt) {
  public Member markAsDeleted() {
    return Member.builder()
        .id(this.id)
        .email(this.email)
        .name(this.name)
        .nickname(this.nickname)
        .profileImage(this.profileImage)
        .oauth2Provider(this.oauth2Provider)
        .bio(this.bio)
        .deleted(true)
        .deletedAt(LocalDateTime.now())
        .build();
  }
}
