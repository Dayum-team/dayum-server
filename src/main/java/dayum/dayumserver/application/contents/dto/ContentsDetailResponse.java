package dayum.dayumserver.application.contents.dto;

import java.time.LocalDateTime;

import dayum.dayumserver.domain.contents.Contents;

public record ContentsDetailResponse(
    long id, long memberId, String memberNickname, String url, LocalDateTime uploadedAt) {

  public static ContentsDetailResponse from(Contents contents) {
    return new ContentsDetailResponse(
        contents.id(),
        contents.member().id(),
        contents.member().nickname(),
        contents.url(),
        contents.createdAt());
  }
}
