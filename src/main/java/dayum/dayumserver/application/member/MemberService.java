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
    public MemberJpaEntity loginOrRegister(NaverUser user) {
        return memberJpaRepository.findByEmail(user.email())
                .orElseGet(() -> memberJpaRepository.save(MemberJpaEntity.builder()
                        .email(user.email())
                        .name(user.name())
                        .nickname(user.name())  // 기본값으로 이름 사용
                        .profileImage(user.profileImage())
                        .loginType("NAVER")
                        .refreshToken(null)
                        .build()));
    }

    // 2. refresh 토큰 저장
    public void updateRefreshToken(Long memberId, String refreshToken) {
        MemberJpaEntity member = memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        member.updateRefreshToken(refreshToken);
        memberJpaRepository.save(member);
    }
}
