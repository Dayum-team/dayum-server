package dayum.dayumserver.infrastructure.repository.jpa.entity;

import static jakarta.persistence.ConstraintMode.*;

import dayum.dayumserver.domain.contents.ContentStatus;
import dayum.dayumserver.infrastructure.repository.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ContentsJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "contents_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "member_id",
      referencedColumnName = "member_id",
      foreignKey = @ForeignKey(NO_CONSTRAINT))
  private MemberJpaEntity member;

  private String title;
  private String description;
  private String thumbnailUrl;
  private String url;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ContentStatus status;
}
