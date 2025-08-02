package dayum.dayumserver.application.contents;

import dayum.dayumserver.application.common.response.PageResponse;
import dayum.dayumserver.application.contents.dto.ContentsResponse;
import dayum.dayumserver.domain.contents.ContentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentsService {

  private final ContentsRepository contentsRepository;

  public PageResponse<ContentsResponse> retrieveNextPage(long cursorId, int size) {
    var contentsList =
        contentsRepository.fetchNextPage(cursorId, size + 1).stream()
            .map(ContentsResponse::from)
            .toList();

    if (contentsList.size() <= size) {
      return new PageResponse<>(contentsList, new PageResponse.PageInfo("", true));
    }
    var items = contentsList.subList(0, size);
    return new PageResponse<>(
        items, new PageResponse.PageInfo(String.valueOf(contentsList.getLast().id()), false));
  }
}
