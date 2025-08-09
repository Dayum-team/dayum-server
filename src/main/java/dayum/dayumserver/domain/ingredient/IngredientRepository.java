package dayum.dayumserver.domain.ingredient;

import java.util.List;

public interface IngredientRepository {

  List<Ingredient> search(String keyword);

  List<Ingredient> findAllBy(List<Long> ingredientIds);
}
