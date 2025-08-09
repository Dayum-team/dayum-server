package dayum.dayumserver.infrastructure.repository.jpa.repository;

import dayum.dayumserver.infrastructure.repository.jpa.entity.IngredientJpaEntity;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientJpaRepository extends JpaRepository<IngredientJpaEntity, Long> {

  List<IngredientJpaEntity> findAllByNameLike(String name, Limit limit);

  Optional<IngredientJpaEntity> findFirstByNameContainingIgnoreCase(String name);
}
