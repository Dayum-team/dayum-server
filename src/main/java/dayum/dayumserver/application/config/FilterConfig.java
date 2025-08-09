package dayum.dayumserver.application.config;

import dayum.dayumserver.application.member.JwtProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

  private final JwtProvider jwtProvider;

  public FilterConfig(JwtProvider jwtProvider) {
    this.jwtProvider = jwtProvider;
  }

  @Bean
  public FilterRegistrationBean<JwtFilter> jwtFilter() {
    FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new JwtFilter(jwtProvider));
    registrationBean.addUrlPatterns("/api/*"); // 필터 적용 경로
    registrationBean.setOrder(1); // 실행 순서 (낮을수록 먼저 실행)
    return registrationBean;
  }
}
