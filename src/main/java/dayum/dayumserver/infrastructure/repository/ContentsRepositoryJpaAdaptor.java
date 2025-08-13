package dayum.dayumserver.infrastructure.repository;

import dayum.dayumserver.domain.contents.Contents;
import dayum.dayumserver.domain.contents.ContentsRepository;
import dayum.dayumserver.infrastructure.repository.jpa.repository.ContentsJpaRepository;
import dayum.dayumserver.infrastructure.repository.mapper.ContentsMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentsRepositoryJpaAdaptor implements ContentsRepository {

  private final ContentsJpaRepository contentsJpaRepository;
  private final ContentsMapper contentsMapper;

  @Override
  public List<Contents> fetchNextPageByMember(long memberId, long cursorId, int size) {
    var page = PageRequest.of(0, size);
    return contentsJpaRepository.findNextPageByMember(memberId, cursorId, page).stream()
        .map(contentsMapper::mapToDomainEntity)
        .toList();
  }

  @Override
  public List<Contents> fetchNextPage(long cursorId, int size) {
    var page = PageRequest.of(0, size);
    return contentsJpaRepository.findNextPage(cursorId, page).stream()
        .map(contentsMapper::mapToDomainEntity)
        .toList();
  }

  @Override
  public Contents fetchBy(long id) {
    return contentsJpaRepository.findById(id).map(contentsMapper::mapToDomainEntity).orElseThrow();
  }

  @Transactional
  @Override
  public Contents save(Contents contents) {
    return contentsMapper.mapToDomainEntity(
        contentsJpaRepository.save(contentsMapper.mapToJpaEntity(contents)));
  }

  @Override
  public void delete(Contents contents) {
    contentsJpaRepository.delete(contentsMapper.mapToJpaEntity(contents));
  }
}
