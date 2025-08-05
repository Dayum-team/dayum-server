package dayum.dayumserver.infrastructure.repository.jpa.entity;

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

  @Column(nullable = false, unique = true)
  private String email;

  private String name;

  private String nickname;

  private String profileImage;

  @Column(nullable = false)
  private String loginType; // NAVER, KAKAO 등

  //  private String refreshToken;

  @Column(length = 1000)
  private String introduce;

  @Builder
  public MemberJpaEntity(
      String email,
      String name,
      String nickname,
      String profileImage,
      String loginType,
      String introduce) {
    this.email = email;
    this.name = name;
    this.nickname = nickname;
    this.profileImage = profileImage;
    this.loginType = loginType;
    //    this.refreshToken = refreshToken;
    this.introduce = introduce;
  }

  //  public void updateRefreshToken(String refreshToken) {
  //    this.refreshToken = refreshToken;
  //  }
}
