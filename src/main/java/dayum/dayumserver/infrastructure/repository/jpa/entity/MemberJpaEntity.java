package dayum.dayumserver.infrastructure.repository.jpa.entity;

import dayum.dayumserver.domain.member.Oauth2Provider;
import dayum.dayumserver.infrastructure.repository.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_id")
  private Long id;

  @Column(nullable = false)
  private String email;

  private String name;

  private String nickname;

  private String profileImage;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Oauth2Provider oauth2Provider;

  @Column(length = 1000)
  private String bio;

  @Builder
  public MemberJpaEntity(
      String email,
      String name,
      String nickname,
      String profileImage,
      Oauth2Provider oauth2Provider,
      String bio) {
    this.email = email;
    this.name = name;
    this.nickname = nickname;
    this.profileImage = profileImage;
    this.oauth2Provider = oauth2Provider;
    this.bio = bio;
  }
}
