package dayum.dayumserver.application.contents;

import dayum.dayumserver.application.contents.dto.ContentsResponse;
import dayum.dayumserver.domain.contents.ContentsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentsService {

  private final ContentsRepository contentsRepository;

  public List<ContentsResponse> retrieveNextPage(long previousId, long size) {
    return contentsRepository.fetchNextPage(previousId, size).stream()
        .map(ContentsResponse::from)
        .toList();
  }
}
