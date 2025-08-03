package dayum.dayumserver.infrastructure.repository.jpa.repository;

import dayum.dayumserver.infrastructure.repository.jpa.entity.ContentsJpaEntity;
import java.util.List;
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
          + "WHERE contents.member.id = :memberId "
          + "AND contents.id >= :cursorId "
          + "ORDER BY contents.id "
          + "LIMIT :size")
  List<ContentsJpaEntity> findNextPageByMember(@Param("memberId") long memberId, @Param("cursorId") long cursorId, @Param("size") int size);

  @Query(
      "SELECT contents "
          + "FROM ContentsJpaEntity contents "
          + "WHERE contents.id >= :cursorId "
          + "ORDER BY contents.id "
          + "LIMIT :size")
  List<ContentsJpaEntity> findNextPage(@Param("cursorId") long cursorId, @Param("size") int size);
}
