package dayum.dayumserver.domain.member;

import dayum.dayumserver.infrastructure.repository.jpa.entity.MemberJpaEntity;
import java.util.Optional;

public interface MemberRepository {

  Optional<MemberJpaEntity> findByEmail(String email);

  boolean existsByNickname(String nickname);

  MemberJpaEntity save(MemberJpaEntity member);
}
