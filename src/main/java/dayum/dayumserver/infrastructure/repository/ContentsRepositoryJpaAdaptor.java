package dayum.dayumserver.infrastructure.repository;

import dayum.dayumserver.domain.contents.Contents;
import dayum.dayumserver.domain.contents.ContentsRepository;
import dayum.dayumserver.infrastructure.repository.jpa.repository.ContentsJpaRepository;
import dayum.dayumserver.infrastructure.repository.mapper.ContentsMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ContentsRepositoryJpaAdaptor implements ContentsRepository {

  private final ContentsJpaRepository contentsJpaRepository;
  private final ContentsMapper contentsMapper;

  @Override
  public List<Contents> fetchNextPage(long previousId, long size) {
    return contentsJpaRepository.findNextPage(previousId, size).stream()
        .map(contentsMapper::mapToDomainEntity)
        .toList();
  }
}
