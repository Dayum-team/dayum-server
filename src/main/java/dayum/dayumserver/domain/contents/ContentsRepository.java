package dayum.dayumserver.domain.contents;

import java.util.List;

public interface ContentsRepository {

  List<Contents> fetchNextPage(long previousId, long size);

  Contents fetchBy(long id);
}
