package dayum.dayumserver.infrastructure.repository;

import dayum.dayumserver.domain.ingredient.IngredientRepository;
import dayum.dayumserver.infrastructure.repository.jpa.repository.IngredientJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IngredientRepositoryJpaAdaptor implements IngredientRepository {

  private final IngredientJpaRepository ingredientJpaRepository;
}
