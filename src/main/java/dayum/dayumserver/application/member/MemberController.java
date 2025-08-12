package dayum.dayumserver.application.member;

import dayum.dayumserver.application.common.response.ApiResponse;
import dayum.dayumserver.application.member.dto.MemberProfileResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {
  private final MemberService memberService;

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
  public ResponseEntity<Boolean> withdraw(HttpServletRequest request) {
    Long memberId = (Long) request.getAttribute("memberId");
    boolean result = memberService.withdraw(memberId);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/me/profile")
  public ApiResponse<MemberProfileResponse> getMyProfile(HttpServletRequest request){
    log.info(String.valueOf(request));
    Long memberId = (Long) request.getAttribute("memberId");
    return ApiResponse.of(memberService.getMemberProfile(memberId));
  }

}
