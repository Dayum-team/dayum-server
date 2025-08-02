package dayum.dayumserver.infrastructure.repository.jpa.repository;

import dayum.dayumserver.infrastructure.repository.jpa.entity.MemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {}
