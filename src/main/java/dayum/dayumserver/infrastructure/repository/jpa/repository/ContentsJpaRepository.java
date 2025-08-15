package dayum.dayumserver.infrastructure.repository.jpa.repository;

import dayum.dayumserver.infrastructure.repository.jpa.entity.ContentsJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentsJpaRepository extends JpaRepository<ContentsJpaEntity, Long> {

  @Query(
      "SELECT contents "
          + "FROM ContentsJpaEntity contents "
          + "LEFT JOIN FETCH contents.member "
          + "LEFT JOIN FETCH contents.ingredients ci "
          + "LEFT JOIN FETCH ci.ingredient "
          + "WHERE contents.member.id = :memberId "
          + "AND contents.status = 'PUBLISHED' "
          + "AND contents.id >= :cursorId "
          + "ORDER BY contents.id")
  List<ContentsJpaEntity> findNextPageByMember(
      @Param("memberId") long memberId, @Param("cursorId") long cursorId, Pageable page);

  @Query(
      "SELECT contents "
          + "FROM ContentsJpaEntity contents "
          + "LEFT JOIN FETCH contents.member "
          + "LEFT JOIN FETCH contents.ingredients ci "
          + "LEFT JOIN FETCH ci.ingredient "
          + "WHERE contents.id >= :cursorId "
          + "AND contents.status = 'PUBLISHED' "
          + "ORDER BY contents.id")
  List<ContentsJpaEntity> findNextPage(@Param("cursorId") long cursorId, Pageable page);

  @Query(
      "SELECT contents "
          + "FROM ContentsJpaEntity contents "
          + "LEFT JOIN FETCH contents.member "
          + "LEFT JOIN FETCH contents.ingredients ci "
          + "LEFT JOIN FETCH ci.ingredient "
          + "WHERE contents.id = :id")
  Optional<ContentsJpaEntity> findById(@Param("id") long id);

  @Query(
      "SELECT contents.id "
          + "FROM ContentsJpaEntity contents "
          + "WHERE NOT EXISTS ("
          + " SELECT 1 "
          + " FROM ContentsIngredientJpaEntity contentIngredient "
          + " WHERE contentIngredient.contents = contents "
          + " AND contentIngredient.ingredient.id NOT IN :ingredientIds"
          + ") "
          + "AND contents.status = 'PUBLISHED' ")
  List<Long> findAllMakeableIds(List<Long> ingredientIds, Pageable page);

  @Query(
      "SELECT contents "
          + "FROM ContentsJpaEntity contents "
          + "LEFT JOIN FETCH contents.member "
          + "LEFT JOIN FETCH contents.ingredients ci "
          + "LEFT JOIN FETCH ci.ingredient "
          + "WHERE contents.id IN :ids")
  List<ContentsJpaEntity> findAllByIdIn(List<Long> ids);
}
