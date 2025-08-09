package dayum.dayumserver.application.ingredient;

import java.util.List;

import dayum.dayumserver.application.ingredient.dto.IngredientResponse;
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
}
