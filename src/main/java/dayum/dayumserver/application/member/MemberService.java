package dayum.dayumserver.application.member;

import dayum.dayumserver.application.member.dto.AppleTokenResponse;
import dayum.dayumserver.application.member.dto.LoginRequest;
import dayum.dayumserver.application.member.dto.LoginResponse;
import dayum.dayumserver.application.member.dto.OAuthUserInfo;
import dayum.dayumserver.application.member.dto.RegisterRequest;
import dayum.dayumserver.client.s3.oauth2.naver.NaverOAuthClient;
import dayum.dayumserver.domain.member.Member;
import dayum.dayumserver.domain.member.MemberRepository;
import dayum.dayumserver.domain.member.Oauth2Provider;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final NaverOAuthClient naverOAuthClient;
  private final AppleAuthService appleAuthService;
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
                    Member.builder()
                        .email(user.email())
                        .name(user.name())
                        .nickname(nickname)
                        .profileImage(profileImage)
                        .oauth2Provider(provider)
                        .bio(bio)
                        .deleted(false)
                        .build()));
  }

  public boolean isNicknameDuplicated(String nickname) {
    return memberRepository.existsByNickname(nickname);
  }

  private OAuthUserInfo resolveOAuthUser(
      Oauth2Provider provider, String accessTokenOrIdToken, String authorizationCode) {

    return switch (provider) {
      case NAVER -> {
        if (isBlank(accessTokenOrIdToken)) {
          throw new IllegalArgumentException("NAVER requires accessToken");
        }
        yield naverOAuthClient.getUserInfo(accessTokenOrIdToken);
      }
      case APPLE -> {
        if (!isBlank(accessTokenOrIdToken)) {
          yield appleAuthService.parseIdTokenToUser(accessTokenOrIdToken);
        }
        if (!isBlank(authorizationCode)) {
          AppleTokenResponse tokens = appleAuthService.exchangeCodeForTokens(authorizationCode);
          yield appleAuthService.parseIdTokenToUser(tokens.id_token());
        }
        throw new IllegalArgumentException(
            "APPLE requires id_token (in accessToken) or authorizationCode");
      }
    };
  }

  private boolean isBlank(String s) {
    return s == null || s.isBlank();
  }

  public Optional<LoginResponse> login(LoginRequest request, Oauth2Provider provider) {
    OAuthUserInfo userInfo =
        resolveOAuthUser(provider, request.accessToken(), request.authorizationCode());

    return memberRepository
        .findByEmailAndProvider(userInfo.email(), provider)
        .filter(m -> !m.deleted())
        .map(
            m ->
                new LoginResponse(
                    jwtProvider.createToken(m.id()), jwtProvider.createRefreshToken(m.id())));
  }

  public LoginResponse signup(RegisterRequest request, Oauth2Provider provider) {
    OAuthUserInfo userInfo =
        resolveOAuthUser(provider, request.accessToken(), request.authorizationCode());

    String nickname =
        Optional.ofNullable(request.nickname())
            .orElseThrow(() -> new IllegalArgumentException("닉네임은 필수 입력 값입니다."));
    String profileImage =
        Optional.ofNullable(request.profileImage()).orElse(userInfo.profileImage());
    String bio = Optional.ofNullable(request.bio()).orElse("");

    Member member = loginOrRegister(userInfo, nickname, profileImage, bio, provider);

    return new LoginResponse(
        jwtProvider.createToken(member.id()), jwtProvider.createRefreshToken(member.id()));
  }

  @Transactional
  public boolean withdraw(long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

    if (member.deleted()) {
      return false;
    }

    memberRepository.save(member.markAsDeleted());
    return true;
  }
}
