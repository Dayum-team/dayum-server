package dayum.dayumserver.infrastructure.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import dayum.dayumserver.domain.contents.ContentsIngredient;
import dayum.dayumserver.infrastructure.repository.jpa.entity.ContentsIngredientJpaEntity;

@Mapper(componentModel = "spring")
public interface ContentsIngredientMapper {

  ContentsIngredientJpaEntity mapToJpaEntity(ContentsIngredient domain);

  @Mapping(target = "contents", ignore = true)
  ContentsIngredient mapToDomainEntity(ContentsIngredientJpaEntity entity);
}
