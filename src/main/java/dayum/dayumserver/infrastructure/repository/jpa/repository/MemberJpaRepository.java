package dayum.dayumserver.infrastructure.repository.jpa.repository;

import dayum.dayumserver.infrastructure.repository.jpa.entity.MemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {
    Optional<MemberJpaEntity> findByEmail(String email);

    boolean existsByNickname(String nickname);

}
