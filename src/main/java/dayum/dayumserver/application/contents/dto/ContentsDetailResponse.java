package dayum.dayumserver.application.contents.dto;

import dayum.dayumserver.domain.contents.Contents;
import java.time.LocalDateTime;

// TODO(chanjun.park): Add fields for nutritional information
public record ContentsDetailResponse(
    long id,
    long memberId,
    String memberNickname,
    String thumbnailUrl,
    String url,
    LocalDateTime uploadedAt) {

  public static ContentsDetailResponse from(Contents contents) {
    return new ContentsDetailResponse(
        contents.id(),
        contents.member().id(),
        contents.member().nickname(),
        contents.url(),
        contents.thumbnailUrl(),
        contents.createdAt());
  }
}
