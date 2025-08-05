package dayum.dayumserver.application.member;

import dayum.dayumserver.application.member.dto.LoginRequest;
import dayum.dayumserver.application.member.dto.LoginResponse;
import dayum.dayumserver.application.member.dto.NaverUser;
import dayum.dayumserver.client.s3.oauth2.naver.NaverOAuthClient;
import dayum.dayumserver.domain.member.Oauth2Provider;
import dayum.dayumserver.infrastructure.repository.jpa.entity.MemberJpaEntity;
import dayum.dayumserver.infrastructure.repository.jpa.repository.MemberJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberJpaRepository memberJpaRepository;
  private final NaverOAuthClient naverOAuthClient;
  private final JwtProvider jwtProvider;

  public MemberJpaEntity loginOrRegister(
      NaverUser user, String nickname, String profileImage, String bio) {
    return memberJpaRepository
        .findByEmail(user.email())
        .orElseGet(
            () ->
                memberJpaRepository.save(
                    MemberJpaEntity.builder()
                        .email(user.email())
                        .name(user.name())
                        .nickname(nickname)
                        .profileImage(profileImage)
                        .oauth2Provider(Oauth2Provider.NAVER)
                        .bio(bio)
                        .build()));
  }

  public boolean isNicknameDuplicated(String nickname) {
    return memberJpaRepository.existsByNickname(nickname);
  }

  public LoginResponse loginWithNaver(LoginRequest request) {
    String accessToken = request.accessToken();
    NaverUser userInfo = naverOAuthClient.getUserInfo(accessToken);

    String nickname = Optional.ofNullable(request.nickname()).orElse(userInfo.name());
    String profileImage =
        Optional.ofNullable(request.profileImage()).orElse(userInfo.profileImage());
    String bio = Optional.ofNullable(request.bio()).orElse("");

    MemberJpaEntity member = loginOrRegister(userInfo, nickname, profileImage, bio);

    String jwt = jwtProvider.createToken(member.getEmail());
    String refresh = jwtProvider.createRefreshToken(member.getEmail());

    return new LoginResponse(jwt, refresh);
  }
}
