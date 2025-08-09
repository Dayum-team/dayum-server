package dayum.dayumserver.application.member;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dayum.dayumserver.application.member.dto.AppleTokenResponse;
import dayum.dayumserver.application.member.dto.OAuthUserInfo;
import dayum.dayumserver.client.s3.oauth2.apple.AppleJwtUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AppleAuthService {

  // 환경값은 yml에서 주입 권장
  @Value("${apple.client-id}")
  private String clientId;

  public AppleTokenResponse exchangeCodeForTokens(String authorizationCode) {
    String clientSecret;
    try {
      clientSecret = AppleJwtUtil.createClientSecret();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create Apple client_secret", e);
    }

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);
    body.add("code", authorizationCode);
    body.add("grant_type", "authorization_code");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    RestTemplate rest = new RestTemplate();
    ResponseEntity<AppleTokenResponse> resp =
        rest.postForEntity(
            "https://appleid.apple.com/auth/token",
            new HttpEntity<>(body, headers),
            AppleTokenResponse.class);
    if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
      throw new IllegalStateException("Apple token API failed: " + resp.getStatusCode());
    }
    return resp.getBody();
  }

  public OAuthUserInfo parseIdTokenToUser(String idToken) {
    try {
      SignedJWT jwt = SignedJWT.parse(idToken);
      JWTClaimsSet c = jwt.getJWTClaimsSet();

      // 빠른 클레임 체크 (서명 검증은 다음 단계에서 추가 권장)
      String sub = c.getSubject(); // 고유 식별자
      String email = c.getStringClaim("email");
      String name = Optional.ofNullable(c.getStringClaim("name")).orElse(email);

      return new OAuthUserInfo(email, name, /* profileImage */ null);
    } catch (Exception e) {
      throw new IllegalStateException("Invalid Apple id_token", e);
    }
  }
}
