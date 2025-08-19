package dayum.dayumserver.infrastructure.repository;

import dayum.dayumserver.domain.contents.Contents;
import dayum.dayumserver.domain.contents.ContentsRepository;
import dayum.dayumserver.domain.ingredient.Ingredient;
import dayum.dayumserver.infrastructure.repository.jpa.repository.ContentsJpaRepository;
import dayum.dayumserver.infrastructure.repository.mapper.ContentsMapper;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
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

  @Override
  public List<Contents> fetchMakeableContents(List<Ingredient> ingredients, int size) {
    if (ingredients == null || ingredients.isEmpty()) {
      return Collections.emptyList();
    }
    var ingredientIds = ingredients.stream().map(Ingredient::id).toList();
    log.info(">> fetchMakeableContents 호출 | 재료 ID 목록: {}", ingredientIds);

    var contentsIds =
        contentsJpaRepository.findAllMakeableIds(ingredientIds, PageRequest.of(0, size));
    log.info(
        "contentsJpaRepository.findAllMakeableIds() 조회 결과 (만들 수 있는 콘텐츠 ID 목록): {}", contentsIds);

    if (contentsIds.isEmpty()) {
      return Collections.emptyList();
    }

    var contentsEntities = contentsJpaRepository.findAllByIdIn(contentsIds);
    log.info(
        "contentsJpaRepository.findAllByIdIn() 조회 결과 (콘텐츠 Entity 개수): {}", contentsEntities.size());

    return contentsEntities.stream().map(contentsMapper::mapToDomainEntity).toList();
  }
}
