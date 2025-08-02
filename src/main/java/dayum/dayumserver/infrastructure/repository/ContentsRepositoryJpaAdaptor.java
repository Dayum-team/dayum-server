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
  public List<Contents> fetchNextPage(long id, int size) {
    return contentsJpaRepository.findNextPage(id, size).stream()
        .map(contentsMapper::mapToDomainEntity)
        .toList();
  }
}
