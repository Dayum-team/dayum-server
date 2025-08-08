package dayum.dayumserver.client.ai.ocr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OcrResponse(List<OcrImageDto> images) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record OcrImageDto(List<OcrFieldDto> fields) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record OcrFieldDto(String inferText) {}
}
