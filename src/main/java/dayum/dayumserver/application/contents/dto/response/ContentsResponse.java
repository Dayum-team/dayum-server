package dayum.dayumserver.application.contents.dto.response;

import dayum.dayumserver.domain.contents.Contents;

public record ContentsResponse(
    long id,
    long memberId,
    String thumbnailUrl,
    String url,
    double calories,
    double carbohydrates,
    double proteins,
    double fats) {

  public static ContentsResponse from(Contents contents) {
    return new ContentsResponse(
        contents.id(),
        contents.member().id(),
        contents.thumbnailUrl(),
        contents.url(),
        contents.calculateCalories(),
        contents.calculateCarbohydrates(),
        contents.calculateProteins(),
        contents.calculateFats());
  }
}
