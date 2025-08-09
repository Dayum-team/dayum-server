package dayum.dayumserver.infrastructure.repository;

import dayum.dayumserver.domain.ingredient.Ingredient;
import dayum.dayumserver.domain.ingredient.IngredientRepository;
import dayum.dayumserver.infrastructure.repository.jpa.repository.IngredientJpaRepository;
import dayum.dayumserver.infrastructure.repository.mapper.IngredientMapper;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IngredientRepositoryJpaAdaptor implements IngredientRepository {

  private final IngredientJpaRepository ingredientJpaRepository;
  private final IngredientMapper ingredientMapper;

  @Override
  public List<Ingredient> search(String keyword) {
    // TODO(chanjun.park): Update it after finalizing the ingredient search policy
    return ingredientJpaRepository.findAllByNameLike("%" + keyword + "%", Limit.of(10)).stream()
        .map(ingredientMapper::mapToDomainEntity)
        .toList();
  }

  @Override
  public List<Ingredient> findAllBy(List<Long> ingredientIds) {
    return ingredientJpaRepository.findAllById(ingredientIds).stream()
        .map(ingredientMapper::mapToDomainEntity)
        .toList();
  }
}
