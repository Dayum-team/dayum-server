package dayum.dayumserver.sample.presentation;

import org.springframework.web.bind.annotation.*;
import dayum.dayumserver.common.response.ApiResponse;

@RestController
@RequestMapping("/api/samples")
public class SampleController {

  @GetMapping("/health")
  public ApiResponse<String> healthCheck() {
    return ApiResponse.of("Sample API is running");
  }
}
