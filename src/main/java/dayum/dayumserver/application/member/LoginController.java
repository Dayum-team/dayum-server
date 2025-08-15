package dayum.dayumserver.application.member;

import dayum.dayumserver.application.member.dto.LoginRequest;
import dayum.dayumserver.application.member.dto.LoginResponse;
import dayum.dayumserver.application.member.dto.RegisterRequest;
import dayum.dayumserver.domain.member.Oauth2Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

  private final MemberService memberService;
  private final JwtProvider jwtProvider;

  @PostMapping("/signup/{provider}")
  public ResponseEntity<LoginResponse> signup(
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
        .login(request, oauth2Provider)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(401).build());
  }

  @GetMapping("/test-token")
  public ResponseEntity<LoginResponse> testToken(@RequestParam Long memberId) {
    return ResponseEntity.ok(
        new LoginResponse(
            jwtProvider.createToken(memberId), jwtProvider.createRefreshToken(memberId)));
  }
}
