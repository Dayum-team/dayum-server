package dayum.dayumserver.infrastructure.repository;

import dayum.dayumserver.domain.ingredient.Ingredient;
import dayum.dayumserver.domain.ingredient.IngredientRepository;
import dayum.dayumserver.infrastructure.repository.jpa.entity.IngredientJpaEntity;
import dayum.dayumserver.infrastructure.repository.jpa.repository.IngredientJpaRepository;
import dayum.dayumserver.infrastructure.repository.mapper.IngredientMapper;
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
  @Override
  public Optional<Ingredient> findByName(String name) {
    // 1. 어떤 이름으로 조회를 시작하는지 확인
    log.info(">> findByName 호출 | name: '{}'", name);

    // 2. DB 조회 결과(JPA Entity)가 있는지 확인
    Optional<IngredientJpaEntity> entityOptional = ingredientJpaRepository.findByName(name);
    log.info("DB 조회 결과 (존재 여부): {}", entityOptional.isPresent() ? "찾음" : "못 찾음");

    // 3. 최종적으로 Domain 객체로 변환된 결과를 반환하는지 확인
    return entityOptional.map(ingredientMapper::mapToDomainEntity);
  }
}
