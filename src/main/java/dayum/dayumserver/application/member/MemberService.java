package dayum.dayumserver.application.member;

import dayum.dayumserver.application.member.dto.NaverUser;
import dayum.dayumserver.domain.member.MemberRepository;
import dayum.dayumserver.infrastructure.repository.jpa.entity.MemberJpaEntity;
import dayum.dayumserver.infrastructure.repository.jpa.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberJpaRepository memberJpaRepository;

    // 1. 로그인 또는 자동 회원가입
    public MemberJpaEntity loginOrRegister(NaverUser user, String nickname, String profileImage, String introduce) {
        return memberJpaRepository.findByEmail(user.email())
                .orElseGet(() -> memberJpaRepository.save(MemberJpaEntity.builder()
                        .email(user.email())
                        .name(user.name())
                        .nickname(nickname)
                        .profileImage(profileImage)
                        .loginType("NAVER")
                        .introduce(introduce)
                        .build()));
    }

    public boolean isNicknameDuplicated(String nickname) {
        return memberJpaRepository.existsByNickname(nickname);
    }
}
