package dayum.dayumserver.client.clova.dto;

import java.util.List;

public record ClovaResponse(Status status, Result result) {
  public record Status(String code, String message) {}

  public record Result(
      Message message,
      String stopReason,
      Integer inputLength,
      Integer outputLength,
      List<AiFilter> aiFilter) {}

  public record Message(String role, String content) {}

  public record AiFilter(String groupName, String name, Double score, String result) {}
}
