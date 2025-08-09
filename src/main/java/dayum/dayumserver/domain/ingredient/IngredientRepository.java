package dayum.dayumserver.domain.ingredient;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository {

  List<Ingredient> search(String keyword);

  Optional<Ingredient> findByName(String name);
}
