package dayum.dayumserver.application.contents;

import dayum.dayumserver.application.common.response.ApiResponse;
import dayum.dayumserver.application.common.response.PageResponse;
import dayum.dayumserver.application.contents.dto.ContentsAnalyzeRequest;
import dayum.dayumserver.application.contents.dto.ContentsAnalyzeResponse;
import dayum.dayumserver.application.contents.dto.ContentsDetailResponse;
import dayum.dayumserver.application.contents.dto.ContentsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
      @RequestParam(value = "member_id", required = false) Long memberId,
      @RequestParam(value = "cursor", defaultValue = "0") long cursorId,
      @RequestParam(value = "size", defaultValue = "15") int size) {
    var contentsPage = contentsService.retrieveNextPage(memberId, cursorId, size);
    return ApiResponse.of(contentsPage);
  }

  @GetMapping("/{id}")
  public ApiResponse<ContentsDetailResponse> retrieveContents(@PathVariable long id) {
    var contents = contentsService.retrieve(id);
    return ApiResponse.of(contents);
  }

  @PostMapping
  public ApiResponse<ContentsAnalyzeResponse> analyzeContents(
      @RequestBody ContentsAnalyzeRequest request) {
    var analyzeResponse =
        contentsService.analyze(request.contentsUrl(), request.memberId());
    return ApiResponse.of(analyzeResponse);
  }
}
