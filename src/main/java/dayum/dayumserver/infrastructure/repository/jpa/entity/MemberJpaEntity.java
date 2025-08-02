package dayum.dayumserver.infrastructure.repository.jpa.entity;

import dayum.dayumserver.infrastructure.repository.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "member_id")
  private Long id;

  private String nickname;

  // TODO: After finalizing the ERD, add the remaining column details.

  @Builder
  public MemberJpaEntity(String nickname) {
    this.nickname = nickname;
  }
}
