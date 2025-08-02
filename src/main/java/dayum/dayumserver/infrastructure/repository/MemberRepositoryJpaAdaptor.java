package dayum.dayumserver.infrastructure.repository;

import dayum.dayumserver.domain.member.MemberRepository;
import dayum.dayumserver.infrastructure.repository.jpa.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryJpaAdaptor implements MemberRepository {

  private final MemberJpaRepository memberJpaRepository;
}
