package dayum.dayumserver.infrastructure.repository.jpa.repository;

import dayum.dayumserver.infrastructure.repository.jpa.entity.IngredientJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientJpaRepository extends JpaRepository<IngredientJpaEntity, Long> {}
