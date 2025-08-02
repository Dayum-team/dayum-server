package dayum.dayumserver.application.contents;

import dayum.dayumserver.application.common.response.ApiResponse;
import dayum.dayumserver.application.contents.dto.ContentsDetailResponse;
import dayum.dayumserver.application.contents.dto.ContentsResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contents")
public class ContentsController {

  private final ContentsService contentsService;

  @GetMapping
  public ApiResponse<List<ContentsResponse>> retrieveAllContents(
      @RequestParam(value = "previous", defaultValue = "0") long previousContentsId,
      @RequestParam("size") long size) {
    var contentsList = contentsService.retrieveNextPage(previousContentsId, size);
    return ApiResponse.of(contentsList);
  }

  @GetMapping("/{id}")
  public ApiResponse<ContentsDetailResponse> retrieveContents(@PathVariable long id) {
    var contents = contentsService.retrieve(id);
    return ApiResponse.of(contents);
  }
}
