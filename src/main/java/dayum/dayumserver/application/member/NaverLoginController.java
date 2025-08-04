package dayum.dayumserver.application.member;

import dayum.dayumserver.application.member.dto.LoginRequest;
import dayum.dayumserver.application.member.dto.LoginResponse;
import dayum.dayumserver.application.member.dto.NaverUser;
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
        String accessToken = request.accessToken();

        // 1. 네이버 유저 정보 가져오기
        NaverUser userInfo = naverOAuthClient.getUserInfo(accessToken);

        // 2. nickname/profileImage/introduce: 프론트 입력값 우선 적용
        String nickname = request.nickname() != null ? request.nickname() : userInfo.name();
        String profileImage = request.profileImage() != null ? request.profileImage() : userInfo.profileImage();
        String introduce = request.introduce() != null ? request.introduce() : "";

        // 3. 회원 조회 or 신규 등록
        var member = memberService.loginOrRegister(userInfo, nickname, profileImage, introduce);

        // 4. JWT 발급
        String jwt = jwtProvider.createToken(member.getEmail());
        String refresh = jwtProvider.createRefreshToken(member.getEmail());

        return ResponseEntity.ok(new LoginResponse(jwt, refresh));
    }
}