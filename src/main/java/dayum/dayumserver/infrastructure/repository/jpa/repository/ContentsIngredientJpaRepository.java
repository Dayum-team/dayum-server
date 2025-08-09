package dayum.dayumserver.infrastructure.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dayum.dayumserver.infrastructure.repository.jpa.entity.ContentsIngredientJpaEntity;

@Repository
public interface ContentsIngredientJpaRepository
    extends JpaRepository<ContentsIngredientJpaEntity, Long> {}
