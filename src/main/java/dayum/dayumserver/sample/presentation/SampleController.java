package dayum.dayumserver.sample.presentation;

import dayum.dayumserver.common.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/samples")
public class SampleController {

  @GetMapping("/health")
  public ApiResponse<String> healthCheck() {
    return ApiResponse.of("Sample API is running");
  }
}
