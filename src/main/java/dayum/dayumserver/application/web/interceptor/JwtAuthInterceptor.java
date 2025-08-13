package dayum.dayumserver.application.web.interceptor;

import dayum.dayumserver.application.common.JwtAuthWhiteList;
import dayum.dayumserver.application.common.exception.AppException;
import dayum.dayumserver.application.common.exception.AuthExceptionCode;
import dayum.dayumserver.application.member.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

  public static final String MEMBER_ID_ATTRIBUTE = "memberId";

  private final JwtProvider jwtProvider;
  private final AntPathMatcher matcher = new AntPathMatcher();
  private static final List<String> TOKEN_AUTHENTICATION_WHITELIST = List.of("/health", "/auth/**");

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    if (isWhitelisted(request.getRequestURI())) {
      return true;
    }
    if (handler instanceof HandlerMethod handlerMethod) {
      if (handlerMethod.hasMethodAnnotation(JwtAuthWhiteList.class)
          || handlerMethod.getBeanType().isAnnotationPresent(JwtAuthWhiteList.class)) {
        return true;
      }
    }

    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new AppException(AuthExceptionCode.UN_AUTHORIZATION);
    }
    String token = authHeader.substring(7);
    if (!jwtProvider.validate(token)) {
      throw new AppException(AuthExceptionCode.UN_AUTHORIZATION);
    }
    Long memberId = jwtProvider.getMemberId(token);
    request.setAttribute(MEMBER_ID_ATTRIBUTE, memberId);
    return true;
  }

  private boolean isWhitelisted(String uri) {
    for (String pattern : TOKEN_AUTHENTICATION_WHITELIST) {
      if (matcher.match(pattern, uri)) return true;
    }
    return false;
  }
}
