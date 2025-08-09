package dayum.dayumserver.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dayum.dayumserver.domain.contents.ContentsIngredient;
import dayum.dayumserver.domain.contents.ContentsIngredientRepository;
import dayum.dayumserver.domain.ingredient.Ingredient;
import dayum.dayumserver.infrastructure.repository.jpa.entity.ContentsIngredientJpaEntity;
import dayum.dayumserver.infrastructure.repository.jpa.repository.ContentsIngredientJpaRepository;

import dayum.dayumserver.infrastructure.repository.mapper.ContentsIngredientMapper;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentsIngredientJpaAdaptor implements ContentsIngredientRepository {

  private final ContentsIngredientJpaRepository ingredientJpaRepository;
  private final ContentsIngredientMapper contentsIngredientMapper;

  @Override
  @Transactional
  public List<ContentsIngredient> saveAll(List<ContentsIngredient> contentsIngredients) {
    List<ContentsIngredientJpaEntity> jpaEntities =
        contentsIngredients.stream().map(contentsIngredientMapper::mapToJpaEntity).toList();

    List<ContentsIngredientJpaEntity> savedJpaEntities =
        ingredientJpaRepository.saveAll(jpaEntities);

    return savedJpaEntities.stream().map(contentsIngredientMapper::mapToDomainEntity).toList();
  }
}
