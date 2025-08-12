package dayum.dayumserver.domain.contents;

import java.util.List;

public interface ContentsRepository {

  List<Contents> fetchNextPageByMember(long memberId, long cursorId, int size);

  List<Contents> fetchNextPage(long cursorId, int size);

  Contents fetchBy(long id);

  Contents save(Contents contents);

  void deleteBy(Contents contents);
}
