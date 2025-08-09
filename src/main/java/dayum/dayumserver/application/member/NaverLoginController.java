package dayum.dayumserver.application.member;

import dayum.dayumserver.application.member.dto.LoginRequest;
import dayum.dayumserver.application.member.dto.LoginResponse;
import dayum.dayumserver.client.s3.oauth2.naver.NaverOAuthClient;
import dayum.dayumserver.domain.member.Oauth2Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class NaverLoginController {

  private final MemberService memberService;
  private final NaverOAuthClient naverOAuthClient;
  private final JwtProvider jwtProvider;

  @PostMapping("/login/{provider}")
  public ResponseEntity<LoginResponse> login(
      @PathVariable String provider, @RequestBody LoginRequest request) {
    Oauth2Provider oauth2Provider = Oauth2Provider.from(provider.toUpperCase());
    LoginResponse response = memberService.login(request, oauth2Provider);
    return ResponseEntity.ok(response);
  }
}
