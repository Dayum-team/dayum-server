package dayum.dayumserver.application.contents;

import dayum.dayumserver.application.common.response.ApiResponse;
import dayum.dayumserver.application.common.response.PageResponse;
import dayum.dayumserver.application.contents.dto.ContentsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contents")
public class ContentsController {

  private final ContentsService contentsService;

  @GetMapping
  public ApiResponse<PageResponse<ContentsResponse>> retrieveAllContents(
      @RequestParam(value = "cursor", defaultValue = "0") long cursorId,
      @RequestParam(value = "size", defaultValue = "15") int size) {
    var contentsPage = contentsService.retrieveNextPage(cursorId, size);
    return ApiResponse.of(contentsPage);
  }
}
