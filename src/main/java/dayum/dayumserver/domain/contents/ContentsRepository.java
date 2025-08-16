package dayum.dayumserver.domain.contents;

import dayum.dayumserver.domain.ingredient.Ingredient;
import java.util.List;

public interface ContentsRepository {

  List<Contents> fetchNextPageByMember(long memberId, long cursorId, int size);

  List<Contents> fetchNextPage(long cursorId, int size);

  Contents fetchBy(long id);

  Contents save(Contents contents);

  void delete(Contents contents);

  List<Contents> fetchMakeableContents(List<Ingredient> ingredients, int size);
}
