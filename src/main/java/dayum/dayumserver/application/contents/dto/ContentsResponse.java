package dayum.dayumserver.application.contents.dto;

import dayum.dayumserver.domain.contents.Contents;

public record ContentsResponse(long id, String url) {

  public static ContentsResponse from(Contents contents) {
    return new ContentsResponse(contents.id(), contents.url());
  }
}
