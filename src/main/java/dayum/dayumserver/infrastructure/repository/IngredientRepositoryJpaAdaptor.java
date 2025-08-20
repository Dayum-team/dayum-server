package dayum.dayumserver.infrastructure.repository;

import dayum.dayumserver.domain.ingredient.Ingredient;
import dayum.dayumserver.domain.ingredient.IngredientRepository;
import dayum.dayumserver.infrastructure.repository.jpa.entity.IngredientJpaEntity;
import dayum.dayumserver.infrastructure.repository.jpa.repository.IngredientJpaRepository;
import dayum.dayumserver.infrastructure.repository.mapper.IngredientMapper;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class IngredientRepositoryJpaAdaptor implements IngredientRepository {

  private final IngredientJpaRepository ingredientJpaRepository;
  private final IngredientMapper ingredientMapper;

  @Override
  public List<Ingredient> search(String keyword) {
    // TODO(chanjun.park): Update it after finalizing the ingredient search policy
    String normalizedKeyword = Normalizer.normalize(keyword, Normalizer.Form.NFC);

    return ingredientJpaRepository
        .findAllByNameLike("%" + normalizedKeyword + "%", Limit.of(10))
        .stream()
        .map(ingredientMapper::mapToDomainEntity)
        .toList();
  }

  @Override
  public List<Ingredient> findAllBy(List<Long> ingredientIds) {
    return ingredientJpaRepository.findAllById(ingredientIds).stream()
        .map(ingredientMapper::mapToDomainEntity)
        .toList();
  }

  @Override
  public Optional<Ingredient> findByName(String name) {
    Optional<IngredientJpaEntity> entityOptional = ingredientJpaRepository.findByName(name);
    return entityOptional.map(ingredientMapper::mapToDomainEntity);
  }

  @Override
  public Optional<Ingredient> findByNameContaining(String name) {
    return ingredientJpaRepository
        .findFirstByNameContainingIgnoreCase(name)
        .map(ingredientMapper::mapToDomainEntity);
  }
}
