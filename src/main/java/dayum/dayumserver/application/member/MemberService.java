package dayum.dayumserver.application.member;

import static dayum.dayumserver.domain.member.Oauth2Provider.APPLE;
import static dayum.dayumserver.domain.member.Oauth2Provider.NAVER;

import dayum.dayumserver.application.member.dto.LoginRequest;
import dayum.dayumserver.application.member.dto.LoginResponse;
import dayum.dayumserver.application.member.dto.NaverUser;
import dayum.dayumserver.client.s3.oauth2.naver.NaverOAuthClient;
import dayum.dayumserver.domain.member.MemberRepository;
import dayum.dayumserver.domain.member.Oauth2Provider;
import dayum.dayumserver.infrastructure.repository.jpa.entity.MemberJpaEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final NaverOAuthClient naverOAuthClient;
  private final JwtProvider jwtProvider;

  public MemberJpaEntity loginOrRegister(
      NaverUser user, String nickname, String profileImage, String bio, Oauth2Provider provider) {
    return memberRepository
        .findByEmail(user.email())
        .orElseGet(
            () ->
                memberRepository.save(
                    MemberJpaEntity.builder()
                        .email(user.email())
                        .name(user.name())
                        .nickname(nickname)
                        .profileImage(profileImage)
                        .oauth2Provider(provider)
                        .bio(bio)
                        .build()));
  }

  public boolean isNicknameDuplicated(String nickname) {
    return memberRepository.existsByNickname(nickname);
  }

  public LoginResponse login(LoginRequest request, Oauth2Provider provider) {
    String accessToken = request.accessToken();

    NaverUser userInfo;
    switch (provider) {
      case NAVER -> userInfo = naverOAuthClient.getUserInfo(accessToken);
      case APPLE -> throw new UnsupportedOperationException("Apple not implemented yet");
      default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
    }

    String nickname = Optional.ofNullable(request.nickname()).orElse(userInfo.name());
    String profileImage =
        Optional.ofNullable(request.profileImage()).orElse(userInfo.profileImage());
    String bio = Optional.ofNullable(request.bio()).orElse("");

    MemberJpaEntity member = loginOrRegister(userInfo, nickname, profileImage, bio, provider);
    String jwt = jwtProvider.createToken(member.getEmail());
    String refresh = jwtProvider.createRefreshToken(member.getEmail());

    return new LoginResponse(jwt, refresh);
  }
}
