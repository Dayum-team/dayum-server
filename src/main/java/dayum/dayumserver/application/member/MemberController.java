package dayum.dayumserver.application.member;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
  private final MemberService memberService;
  private final JwtProvider jwtProvider;

  @GetMapping
  public ResponseEntity<List<String>> checkNickname(@RequestParam String nickname) {
    boolean isDuplicated = memberService.isNicknameDuplicated(nickname);

    if (isDuplicated) {
      return ResponseEntity.ok(List.of(nickname));
    } else {
      return ResponseEntity.ok(Collections.emptyList());
    }
  }

  @DeleteMapping
  public ResponseEntity<Boolean> withdraw(@RequestHeader("Authorization") String authHeader) {
    String token = authHeader.replace("Bearer ", "");
    Long memberId = jwtProvider.getMemberId(token);

    boolean result = memberService.withdraw(memberId);
    return ResponseEntity.ok(result);
  }
}
