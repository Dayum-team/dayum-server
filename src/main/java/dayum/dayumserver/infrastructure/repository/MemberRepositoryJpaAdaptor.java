package dayum.dayumserver.infrastructure.repository;

import dayum.dayumserver.domain.member.MemberRepository;
import dayum.dayumserver.infrastructure.repository.jpa.entity.MemberJpaEntity;
import dayum.dayumserver.infrastructure.repository.jpa.repository.MemberJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryJpaAdaptor implements MemberRepository {

  private final MemberJpaRepository memberJpaRepository;

  @Override
  public Optional<MemberJpaEntity> findByEmail(String email) {
    return memberJpaRepository.findByEmail(email);
  }

  @Override
  public boolean existsByNickname(String nickname) {
    return memberJpaRepository.existsByNickname(nickname);
  }

  @Override
  public MemberJpaEntity save(MemberJpaEntity member) {
    return memberJpaRepository.save(member);
  }
}
