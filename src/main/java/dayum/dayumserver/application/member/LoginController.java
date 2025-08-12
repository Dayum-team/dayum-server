package dayum.dayumserver.application.member;

import dayum.dayumserver.application.member.dto.LoginRequest;
import dayum.dayumserver.application.member.dto.LoginResponse;
import dayum.dayumserver.application.member.dto.RegisterRequest;
import dayum.dayumserver.client.s3.oauth2.naver.NaverOAuthClient;
import dayum.dayumserver.domain.member.Oauth2Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class LoginController {

  private final MemberService memberService;

  @PostMapping("/signup/{provider}")
  public ResponseEntity<LoginResponse> login(
      @PathVariable String provider, @RequestBody RegisterRequest request) {
    Oauth2Provider oauth2Provider = Oauth2Provider.from(provider.toUpperCase());
    LoginResponse response = memberService.signup(request, oauth2Provider);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/login/{provider}")
  public ResponseEntity<LoginResponse> login(
      @PathVariable String provider, @RequestBody LoginRequest request) {
    Oauth2Provider oauth2Provider = Oauth2Provider.from(provider.toUpperCase());

    return memberService
        .login(request.accessToken(), oauth2Provider)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(401).build()); // 미가입 or 탈퇴 -> 401
  }
}
