package dayum.dayumserver.application.web.filter;

import dayum.dayumserver.application.member.JwtProvider;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
@RequiredArgsConstructor
public class JwtFilter implements Filter {

  private final JwtProvider jwtProvider;
  private final AntPathMatcher matcher = new AntPathMatcher();
  private static final List<String> TOKEN_AUTHENTICATION_WHITELIST =
      List.of("/health", "/auth/**", "/api/contents/**");

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;

    String uri = req.getRequestURI();

    if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
      chain.doFilter(request, response);
      return;
    }

    if (isWhitelisted(uri)) {
      chain.doFilter(request, response);
      return;
    }

    String authHeader = req.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      unauthorized(res, "Missing or invalid Authorization header");
      return;
    }

    String token = authHeader.substring(7);
    if (!jwtProvider.validate(token)) {
      unauthorized(res, "Invalid or expired token");
      return;
    }

    Long memberId = jwtProvider.getMemberId(token);
    req.setAttribute("memberId", memberId);

    chain.doFilter(request, response);
  }

  private boolean isWhitelisted(String uri) {
    for (String pattern : TOKEN_AUTHENTICATION_WHITELIST) {
      if (matcher.match(pattern, uri)) return true;
    }
    return false;
  }

  private void unauthorized(HttpServletResponse res, String msg) throws IOException {
    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    res.setContentType("application/json;charset=UTF-8");
    res.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"" + msg + "\"}");
  }
}
