package dayum.dayumserver.client.s3.oauth2.naver;

import dayum.dayumserver.application.member.dto.OAuthUserInfo;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class NaverOAuthClient {

  private static final String NAVER_USERINFO_URL = "https://openapi.naver.com/v1/nid/me";

  private final RestTemplate restTemplate = new RestTemplate();

  public OAuthUserInfo getUserInfo(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);

    HttpEntity<?> request = new HttpEntity<>(headers);

    ResponseEntity<Map> response =
        restTemplate.exchange(NAVER_USERINFO_URL, HttpMethod.GET, request, Map.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      log.error("Failed to get user info from Naver. status={}", response.getStatusCode());
      throw new RuntimeException("네이버 유저 정보를 가져오는데 실패했습니다.");
    }

    Map<String, Object> body = response.getBody();
    Map<String, Object> responseData = (Map<String, Object>) body.get("response");

    String email = (String) responseData.get("email");
    String name = (String) responseData.get("name");
    String profileImage = (String) responseData.get("profile_image");

    return new OAuthUserInfo(email, name, profileImage);
  }
}
