package dayum.dayumserver.domain.contents;

import java.util.List;

public interface ContentsIngredientRepository {

  List<ContentsIngredient> saveAll(List<ContentsIngredient> contentsIngredients);
}
