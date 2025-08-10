package dayum.dayumserver.application.contents.dto.response;

import dayum.dayumserver.domain.contents.Contents;

public record ContentsResponse(
    long id,
    String thumbnailUrl,
    String url,
    double calories,
    double carbohydrates,
    double proteins,
    double fats) {

  public static ContentsResponse from(Contents contents) {
    return new ContentsResponse(
        contents.id(),
        contents.url(),
        contents.thumbnailUrl(),
        contents.calculateCalories(),
        contents.calculateCarbohydrates(),
        contents.calculateProteins(),
        contents.calculateFats());
  }
}
