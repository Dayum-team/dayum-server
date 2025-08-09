package dayum.dayumserver.infrastructure.repository.jpa.repository;

import dayum.dayumserver.domain.member.Oauth2Provider;
import dayum.dayumserver.infrastructure.repository.jpa.entity.MemberJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {
  Optional<MemberJpaEntity> findByEmailAndOauth2Provider(String email, Oauth2Provider provider);

  boolean existsByNickname(String nickname);
}
