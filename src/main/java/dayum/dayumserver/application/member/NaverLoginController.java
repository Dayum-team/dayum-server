package dayum.dayumserver.application.member;

import dayum.dayumserver.application.member.dto.LoginRequest;
import dayum.dayumserver.application.member.dto.LoginResponse;
import dayum.dayumserver.application.member.dto.NaverUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/naver")
@RequiredArgsConstructor
public class NaverLoginController {

    private final MemberService memberService;
    private final NaverOAuthClient naverOAuthClient;
    private final JwtProvider jwtProvider;
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        String accessToken = request.accessToken();

        // 1. 네이버 유저 정보 가져오기
        NaverUser userInfo = naverOAuthClient.getUserInfo(accessToken);

        // 2. 회원 조회 or 신규 등록
        var member = memberService.loginOrRegister(userInfo);

        // 3. JWT 발급
        String jwt = jwtProvider.createToken(member.getEmail());
        String refresh = jwtProvider.createRefreshToken(member.getEmail());

        // 4. refresh 토큰 DB 저장
        memberService.updateRefreshToken(member.getId(), refresh);

        // 5. 응답 반환
        return ResponseEntity.ok(new LoginResponse(jwt, refresh));
    }
}
