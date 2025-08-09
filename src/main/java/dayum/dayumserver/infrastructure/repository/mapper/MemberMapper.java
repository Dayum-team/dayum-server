package dayum.dayumserver.infrastructure.repository.mapper;

import org.mapstruct.Mapper;

import dayum.dayumserver.domain.member.Member;
import dayum.dayumserver.infrastructure.repository.jpa.entity.MemberJpaEntity;

@Mapper(componentModel = "spring")
public interface MemberMapper {

  Member mapToDomainEntity(MemberJpaEntity memberJpaEntity);
}
