package dayum.dayumserver.application.config;

import dayum.dayumserver.application.member.JwtProvider;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

public class JwtFilter implements Filter {

  private final JwtProvider jwtProvider;

  public JwtFilter(JwtProvider jwtProvider) {
    this.jwtProvider = jwtProvider;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String authHeader = httpRequest.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);

      if (jwtProvider.validate(token)) {
        Long memberId = jwtProvider.getMemberId(token);
        // 요청 속성에 memberId 저장 → 컨트롤러에서 꺼낼 수 있음
        httpRequest.setAttribute("memberId", memberId);
      }
    }

    chain.doFilter(request, response);
  }
}
