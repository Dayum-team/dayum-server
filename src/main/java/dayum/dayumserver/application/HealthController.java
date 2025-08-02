package dayum.dayumserver.application;

import dayum.dayumserver.application.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  @GetMapping("/health")
  public ApiResponse<String> healthCheck() {
    return ApiResponse.of("Sample API is running");
  }
}
