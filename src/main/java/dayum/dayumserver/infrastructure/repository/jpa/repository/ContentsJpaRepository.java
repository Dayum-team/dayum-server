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
          + "JOIN FETCH contents.member "
          + "JOIN FETCH contents.ingredients ci "
          + "JOIN FETCH ci.ingredient "
          + "WHERE contents.member.id = :memberId "
          + "AND contents.id >= :cursorId "
          + "ORDER BY contents.id")
  List<ContentsJpaEntity> findNextPageByMember(
      @Param("memberId") long memberId, @Param("cursorId") long cursorId, Pageable page);

  @Query(
      "SELECT contents "
          + "FROM ContentsJpaEntity contents "
          + "JOIN FETCH contents.member "
          + "JOIN FETCH contents.ingredients ci "
          + "JOIN FETCH ci.ingredient "
          + "WHERE contents.id >= :cursorId "
          + "ORDER BY contents.id")
  List<ContentsJpaEntity> findNextPage(@Param("cursorId") long cursorId, Pageable page);

  @Query(
      "SELECT contents "
          + "FROM ContentsJpaEntity contents "
          + "JOIN FETCH contents.member "
          + "JOIN FETCH contents.ingredients ci "
          + "JOIN FETCH ci.ingredient "
          + "WHERE contents.id = :id")
  Optional<ContentsJpaEntity> findById(@Param("id") long id);
}
