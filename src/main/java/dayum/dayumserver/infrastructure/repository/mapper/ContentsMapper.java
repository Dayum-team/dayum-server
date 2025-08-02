package dayum.dayumserver.infrastructure.repository.mapper;

import dayum.dayumserver.domain.contents.Contents;
import dayum.dayumserver.infrastructure.repository.jpa.entity.ContentsJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContentsMapper {

  Contents mapToDomainEntity(ContentsJpaEntity contentsJpaEntity);
}
