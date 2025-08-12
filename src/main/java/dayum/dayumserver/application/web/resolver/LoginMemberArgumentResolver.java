package dayum.dayumserver.application.web.resolver;

import dayum.dayumserver.application.common.JwtAuth;
import dayum.dayumserver.application.common.exception.AppException;
import dayum.dayumserver.application.common.exception.AuthExceptionCode;
import dayum.dayumserver.application.common.request.LoginMember;
import dayum.dayumserver.application.web.interceptor.JwtAuthInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(JwtAuth.class)
        && parameter.getParameterType().equals(LoginMember.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    return Optional.ofNullable(webRequest.getNativeRequest(HttpServletRequest.class))
        .map(request -> request.getAttribute(JwtAuthInterceptor.MEMBER_ID_ATTRIBUTE))
        .map(memberId -> new LoginMember((Long) memberId))
        .orElseThrow(() -> new AppException(AuthExceptionCode.UN_AUTHORIZATION));
  }
}
