package dayum.dayumserver.application.web.interceptor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dayum.dayumserver.application.web.filter.CachedBodyWrappingFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {

  private static final Logger API_LOGGER = Logger.getLogger("api.access");
  private static final ObjectMapper objectMapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
      throws Exception {
    long startTime =
        (long)
            Optional.ofNullable(request.getAttribute(CachedBodyWrappingFilter.ATTR_START_TIME))
                .orElse(System.currentTimeMillis());
    long took = System.currentTimeMillis() - startTime;
    String requestId = (String) request.getAttribute(CachedBodyWrappingFilter.ATTR_REQUEST_ID);

    ContentCachingRequestWrapper requestWrapper =
        (request instanceof ContentCachingRequestWrapper)
            ? (ContentCachingRequestWrapper) request
            : null;
    ContentCachingResponseWrapper responseWrapper =
        (response instanceof ContentCachingResponseWrapper)
            ? (ContentCachingResponseWrapper) response
            : null;

    Map<String, Object> apiLog = new LinkedHashMap<>();
    apiLog.put("@timestamp", new Date());
    apiLog.put("requestId", requestId);
    apiLog.put("method", request.getMethod());
    apiLog.put("path", request.getRequestURI());
    apiLog.put("query", Optional.ofNullable(request.getQueryString()).orElse(""));
    apiLog.put("status", response.getStatus());
    apiLog.put("duration_ms", took);

    String requestBody =
        requestWrapper != null
            ? new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8)
            : "";
    String responseBody =
        responseWrapper != null
            ? new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8)
            : "";
    apiLog.put("requestBody", requestBody);
    apiLog.put("responseBody", responseBody);

    if (ex != null) {
      apiLog.put(
          "error",
          Map.of(
              "type", ex.getClass().getName(),
              "message", ex.getMessage()));
    }
    API_LOGGER.info(objectMapper.writeValueAsString(apiLog));
  }
}
