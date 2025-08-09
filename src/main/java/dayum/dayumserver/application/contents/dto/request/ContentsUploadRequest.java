package dayum.dayumserver.application.contents.dto.request;

import java.util.List;

public record ContentsUploadRequest(List<IngredientDto> ingredients) {
  public record IngredientDto(Long id, Long quantity) {}
}
