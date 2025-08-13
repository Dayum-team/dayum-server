package dayum.dayumserver.application.contents.dto.response;

import dayum.dayumserver.domain.contents.Contents;
import java.time.LocalDateTime;

public record ContentsDetailResponse(
    long id,
    long memberId,
    String memberNickname,
    String thumbnailUrl,
    String url,
    double calories,
    double carbohydrates,
    double proteins,
    double fats,
    LocalDateTime uploadedAt) {

  public static ContentsDetailResponse from(Contents contents) {
    return new ContentsDetailResponse(
        contents.id(),
        contents.member().id(),
        contents.member().nickname(),
        contents.thumbnailUrl(),
        contents.url(),
        contents.calculateCalories(),
        contents.calculateCarbohydrates(),
        contents.calculateProteins(),
        contents.calculateFats(),
        contents.createdAt());
  }
}
