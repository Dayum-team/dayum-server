package dayum.dayumserver.infrastructure.repository.mapper;

import dayum.dayumserver.domain.ingredient.Ingredient;
import dayum.dayumserver.infrastructure.repository.jpa.entity.IngredientJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IngredientMapper {

  Ingredient mapToDomainEntity(IngredientJpaEntity ingredientJpaEntity);
}
