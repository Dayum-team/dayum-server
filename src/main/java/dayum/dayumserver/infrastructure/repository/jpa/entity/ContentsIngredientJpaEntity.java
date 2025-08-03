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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contents_ingredients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentsIngredientJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "contents_ingredient_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "contents_id",
      referencedColumnName = "contents_id",
      foreignKey = @ForeignKey(NO_CONSTRAINT))
  private ContentsJpaEntity contents;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "ingredient_id",
      referencedColumnName = "ingredient_id",
      foreignKey = @ForeignKey(NO_CONSTRAINT))
  private IngredientJpaEntity ingredient;

  private Long quantity;
}
