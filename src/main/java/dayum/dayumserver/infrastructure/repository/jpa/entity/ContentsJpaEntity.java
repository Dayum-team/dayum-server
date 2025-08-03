package dayum.dayumserver.infrastructure.repository.jpa.entity;

import static jakarta.persistence.ConstraintMode.*;

import dayum.dayumserver.infrastructure.repository.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentsJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "contents_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", referencedColumnName = "member_id", foreignKey = @ForeignKey(NO_CONSTRAINT))
  private MemberJpaEntity member;

  private String title;
  private String description;
  private String url;

  @Builder
  public ContentsJpaEntity(MemberJpaEntity member, String title, String description, String url) {
    this.member = member;
    this.title = title;
    this.description = description;
    this.url = url;
  }
}
