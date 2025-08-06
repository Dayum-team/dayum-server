package dayum.dayumserver.domain.member;

import java.time.LocalDateTime;

// TODO: After finalizing the ERD, add the remaining column details.
public record Member(
    Long id,
    String email,
    String name,
    String nickname,
    String profileImage,
    Oauth2Provider oauth2Provider,
    String bio,
    boolean deleted,
    LocalDateTime deletedAt) {}
