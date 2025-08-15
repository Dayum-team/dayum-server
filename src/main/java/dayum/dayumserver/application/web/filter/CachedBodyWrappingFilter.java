package dayum.dayumserver.application.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.jboss.logging.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CachedBodyWrappingFilter extends OncePerRequestFilter {

  public static final String ATTR_START_TIME = "api.log.startTime";
  public static final String ATTR_REQUEST_ID = "api.log.requestId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    ContentCachingRequestWrapper requestWrapper =
        (request instanceof ContentCachingRequestWrapper)
            ? (ContentCachingRequestWrapper) request
            : new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper responseWrapper =
        (response instanceof ContentCachingResponseWrapper)
            ? (ContentCachingResponseWrapper) response
            : new ContentCachingResponseWrapper(response);

    var requestId = UUID.randomUUID().toString();
    request.setAttribute(ATTR_START_TIME, System.currentTimeMillis());
    request.setAttribute(ATTR_REQUEST_ID, requestId);

    MDC.put("requestId", requestId);
    try {
      filterChain.doFilter(requestWrapper, responseWrapper);
    } finally {
      responseWrapper.copyBodyToResponse();
      MDC.remove("requestId");
    }
  }
}
