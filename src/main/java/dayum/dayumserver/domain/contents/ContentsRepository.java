package dayum.dayumserver.domain.contents;

import java.util.List;

public interface ContentsRepository {

  List<Contents> fetchNextPage(long cursorId, int size);

  Contents fetchBy(long id);
}
