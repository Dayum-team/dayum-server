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
@Table(name = "ingredients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IngredientJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "ingredient_id")
  private Long id;

  private String name;
  private String standardQuantity;
  private Double calories;
  private Double carbohydrates;
  private Double proteins;
  private Double fats;
  private Double sugars;
  private Double sodium;

  @Builder
  public IngredientJpaEntity(
      String name,
      String standardQuantity,
      Double calories,
      Double carbohydrates,
      Double proteins,
      Double fats,
      Double sugars,
      Double sodium) {
    this.name = name;
    this.standardQuantity = standardQuantity;
    this.calories = calories;
    this.carbohydrates = carbohydrates;
    this.proteins = proteins;
    this.fats = fats;
    this.sugars = sugars;
    this.sodium = sodium;
  }
}
