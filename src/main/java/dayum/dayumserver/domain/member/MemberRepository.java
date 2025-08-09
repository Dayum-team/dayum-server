package dayum.dayumserver.domain.member;

import java.util.Optional;

public interface MemberRepository {

  boolean existsByNickname(String nickname);

  Member save(Member member);

  Optional<Member> findByEmailAndProvider(String email, Oauth2Provider provider);

  Optional<Member> findById(Long id);

  Member fetchBy(long memberId);
}
