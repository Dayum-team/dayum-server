package dayum.dayumserver.application.ingredient;

import dayum.dayumserver.application.common.response.ApiResponse;
import dayum.dayumserver.application.ingredient.dto.IngredientResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ingredients")
public class IngredientController {

  private final IngredientService ingredientService;

  @GetMapping
  public ApiResponse<List<IngredientResponse>> searchIngredients(@RequestParam String keyword) {
    var ingredients = ingredientService.search(keyword);
    return ApiResponse.of(ingredients);
  }
}
