package dayum.dayumserver.application.member;

import static dayum.dayumserver.domain.member.Oauth2Provider.APPLE;
import static dayum.dayumserver.domain.member.Oauth2Provider.NAVER;

import dayum.dayumserver.application.member.dto.LoginRequest;
import dayum.dayumserver.application.member.dto.LoginResponse;
import dayum.dayumserver.application.member.dto.OAuthUserInfo;
import dayum.dayumserver.client.s3.oauth2.naver.NaverOAuthClient;
import dayum.dayumserver.domain.member.Member;
import dayum.dayumserver.domain.member.MemberRepository;
import dayum.dayumserver.domain.member.Oauth2Provider;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final NaverOAuthClient naverOAuthClient;
  private final JwtProvider jwtProvider;

  public Member loginOrRegister(
      OAuthUserInfo user,
      String nickname,
      String profileImage,
      String bio,
      Oauth2Provider provider) {

    return memberRepository
        .findByEmailAndProvider(user.email(), provider)
        .orElseGet(
            () ->
                memberRepository.save(
                    new Member(
                        null,
                        user.email(),
                        user.name(),
                        nickname,
                        profileImage,
                        provider,
                        bio,
                        false,
                        null)));
  }

  public boolean isNicknameDuplicated(String nickname) {
    return memberRepository.existsByNickname(nickname);
  }

  public LoginResponse login(LoginRequest request, Oauth2Provider provider) {
    String accessToken = request.accessToken();

    OAuthUserInfo userInfo =
        switch (provider) {
          case NAVER -> naverOAuthClient.getUserInfo(accessToken);
          case APPLE -> throw new UnsupportedOperationException("Apple not implemented yet");
        };

    String nickname = Optional.ofNullable(request.nickname()).orElse(userInfo.name());
    String profileImage =
        Optional.ofNullable(request.profileImage()).orElse(userInfo.profileImage());
    String bio = Optional.ofNullable(request.bio()).orElse("");

    Member member = loginOrRegister(userInfo, nickname, profileImage, bio, provider);
    String jwt = jwtProvider.createToken(member.id());
    String refresh = jwtProvider.createRefreshToken(member.id());

    return new LoginResponse(jwt, refresh);
  }

  @Transactional
  public boolean withdraw(long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

    if (member.deleted()) {
      return false; // 이미 탈퇴 상태
    }

    Member updated =
        new Member(
            member.id(),
            member.email(),
            member.name(),
            member.nickname(),
            member.profileImage(),
            member.oauth2Provider(),
            member.bio(),
            true, // deleted
            LocalDateTime.now());

    memberRepository.save(updated);
    return true;
  }
}
