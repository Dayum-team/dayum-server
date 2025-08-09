package dayum.dayumserver.application.member;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dayum.dayumserver.application.member.dto.AppleTokenResponse;
import dayum.dayumserver.application.member.dto.OAuthUserInfo;
import dayum.dayumserver.client.s3.oauth2.apple.AppleJwtUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class AppleAuthService {

  @Value("${apple.client-id}")
  private String clientId;

  private final AppleJwtUtil appleJwtUtil;
  private final RestClient restClient;

  public AppleTokenResponse exchangeCodeForTokens(String authorizationCode) {
    final String clientSecret;
    try {
      clientSecret = appleJwtUtil.createClientSecret();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create Apple client_secret", e);
    }

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);
    body.add("code", authorizationCode);
    body.add("grant_type", "authorization_code");

    return restClient
        .post()
        .uri("https://appleid.apple.com/auth/token")
        .contentType(
            MediaType.APPLICATION_FORM_URLENCODED) // application/json → form-urlencoded로 오버라이드
        .body(body)
        .retrieve()
        .body(AppleTokenResponse.class);
  }

  public OAuthUserInfo parseIdTokenToUser(String idToken) {
    try {
      SignedJWT jwt = SignedJWT.parse(idToken);
      JWTClaimsSet c = jwt.getJWTClaimsSet();

      String email = c.getStringClaim("email");
      String name = Optional.ofNullable(c.getStringClaim("name")).orElse(email);
      return new OAuthUserInfo(email, name, null);
    } catch (Exception e) {
      throw new IllegalStateException("Invalid Apple id_token", e);
    }
  }
}
