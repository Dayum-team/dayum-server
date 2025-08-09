package dayum.dayumserver.infrastructure.repository.jpa.entity;

import dayum.dayumserver.domain.member.Oauth2Provider;
import dayum.dayumserver.infrastructure.repository.jpa.BaseEntity;
import jakarta.persistence.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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

  private LocalDateTime deletedAt;

  private boolean deleted;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Oauth2Provider oauth2Provider;

  @Column(length = 1000)
  private String bio;

  @Builder
  public MemberJpaEntity(
      Long id,
      String email,
      String name,
      String nickname,
      String profileImage,
      Oauth2Provider oauth2Provider,
      String bio,
      LocalDateTime deletedAt,
      boolean deleted) {
    this.id = id;
    this.email = email;
    this.name = name;
    this.nickname = nickname;
    this.profileImage = profileImage;
    this.oauth2Provider = oauth2Provider;
    this.bio = bio;
    this.deletedAt = deletedAt;
    this.deleted = deleted;
  }
}
