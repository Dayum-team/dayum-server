package dayum.dayumserver.application.member;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
  private final MemberService memberService;

  @GetMapping("/nickname/duplication")
  public ResponseEntity<List<String>> checkNickname(@RequestParam String nickname) {
    boolean isDuplicated = memberService.isNicknameDuplicated(nickname);

    if (isDuplicated) {
      return ResponseEntity.ok(List.of(nickname));
    } else {
      return ResponseEntity.ok(Collections.emptyList());
    }
  }
}
