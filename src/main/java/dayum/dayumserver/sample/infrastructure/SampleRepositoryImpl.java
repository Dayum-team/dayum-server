package dayum.dayumserver.sample.infrastructure;

import dayum.dayumserver.sample.domain.SampleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SampleRepositoryImpl implements SampleRepository {

  private final SampleJpaRepository sampleJpaRepository;
}
