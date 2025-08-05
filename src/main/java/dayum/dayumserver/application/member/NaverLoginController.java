package dayum.dayumserver.application.member;

import dayum.dayumserver.application.member.dto.LoginRequest;
import dayum.dayumserver.application.member.dto.LoginResponse;
import dayum.dayumserver.client.s3.oauth2.naver.NaverOAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth/naver")
@RequiredArgsConstructor
public class NaverLoginController {

  private final MemberService memberService;
  private final NaverOAuthClient naverOAuthClient;
  private final JwtProvider jwtProvider;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    LoginResponse response = memberService.loginWithNaver(request);
    return ResponseEntity.ok(response);
  }
}
