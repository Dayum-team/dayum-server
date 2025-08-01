package dayum.dayumserver.sample.infrastructure;

import dayum.dayumserver.sample.domain.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SampleJpaRepository extends JpaRepository<Sample, Long> {}
