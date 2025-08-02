package dayum.dayumserver.infrastructure.repository;

import dayum.dayumserver.domain.contents.ContentsRepository;
import dayum.dayumserver.infrastructure.repository.jpa.repository.ContentsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ContentsRepositoryJpaAdaptor implements ContentsRepository {

  private final ContentsJpaRepository contentsJpaRepository;
}
