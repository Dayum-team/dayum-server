package dayum.dayumserver.infrastructure.repository.mapper;

import dayum.dayumserver.domain.contents.Contents;
import dayum.dayumserver.infrastructure.repository.jpa.entity.ContentsJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContentsMapper {

  Contents mapToDomainEntity(ContentsJpaEntity contentsJpaEntity);

  ContentsJpaEntity mapToJpaEntity(Contents contents);
}
