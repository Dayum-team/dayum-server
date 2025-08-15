package dayum.dayumserver.application.contents;

import dayum.dayumserver.application.common.response.ApiResponse;
import dayum.dayumserver.application.contents.dto.request.ContentsRecommendRequest;
import dayum.dayumserver.application.contents.dto.response.ContentsDetailResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/api")
public class ContentsInternalController {

  private final ContentsService contentsService;

  @PostMapping("/recommended")
  public ApiResponse<List<ContentsDetailResponse>> recommendContents(
      @RequestBody ContentsRecommendRequest request) {
    var contents = contentsService.recommend(request);
    return ApiResponse.of(contents);
  }
}
