package dayum.dayumserver.application.ingredient;

import dayum.dayumserver.application.contents.dto.internal.ExtractedIngredientData;
import dayum.dayumserver.application.ingredient.dto.IngredientResponse;
import dayum.dayumserver.domain.ingredient.Ingredient;
import dayum.dayumserver.domain.ingredient.IngredientRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IngredientService {

  private final IngredientRepository ingredientRepository;

  public List<IngredientResponse> search(String keyword) {
    return ingredientRepository.search(keyword).stream().map(IngredientResponse::from).toList();
  }

  public List<Ingredient> findIngredientsByNames(List<ExtractedIngredientData> analysisResult) {
    return analysisResult.stream()
        .map(data -> ingredientRepository.searchByName(data.name()))
        .flatMap(Optional::stream)
        .collect(Collectors.toList());
  }
}
