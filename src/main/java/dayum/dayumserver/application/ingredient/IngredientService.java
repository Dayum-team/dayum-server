package dayum.dayumserver.application.ingredient;

import java.util.List;
import java.util.Optional;

import dayum.dayumserver.application.ingredient.dto.IngredientResponse;
import dayum.dayumserver.domain.ingredient.Ingredient;
import dayum.dayumserver.domain.ingredient.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IngredientService {

  private final IngredientRepository ingredientRepository;

  public List<IngredientResponse> search(String keyword) {
    return ingredientRepository.search(keyword).stream().map(IngredientResponse::from).toList();
  }

  public List<Ingredient> findIngredientsByNames(List<String> ingredientNames) {
    return ingredientNames.stream()
        .map(ingredientRepository::findByName)
        .flatMap(Optional::stream)
        .toList();
  }

  public List<Ingredient> findIngredientsByNamesContaining(List<String> ingredientNames) {
    return ingredientNames.stream()
        .map(ingredientRepository::findByNameContaining)
        .flatMap(Optional::stream)
        .toList();
  }

  public List<Ingredient> findAllByIds(List<Long> ingredientIds) {
    return ingredientRepository.findAllBy(ingredientIds);
  }
}
