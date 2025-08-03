package dayum.dayumserver.application.contents;

import dayum.dayumserver.application.common.response.PageResponse;
import dayum.dayumserver.application.contents.dto.ContentsDetailResponse;
import dayum.dayumserver.application.contents.dto.ContentsResponse;
import dayum.dayumserver.domain.contents.ContentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentsService {

  private final ContentsRepository contentsRepository;

  public PageResponse<ContentsResponse> retrieveNextPage(Long memberId, long cursorId, int size) {
    var contentsList =
        switch (memberId) {
          case null -> contentsRepository.fetchNextPage(cursorId, size + 1);
          default -> contentsRepository.fetchNextPageByMember(memberId, cursorId, size + 1);
        };

    if (contentsList.size() <= size) {
      var items = contentsList.stream().map(ContentsResponse::from).toList();
      return new PageResponse<>(items, new PageResponse.PageInfo("", true));
    }
    var items = contentsList.subList(0, size).stream().map(ContentsResponse::from).toList();
    return new PageResponse<>(
        items, new PageResponse.PageInfo(String.valueOf(items.getLast().id()), false));
  }

  public ContentsDetailResponse retrieve(long id) {
    var contents = contentsRepository.fetchBy(id);
    return ContentsDetailResponse.from(contents);
  }
}
