package dayum.dayumserver.client.ai.ocr;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class OcrService {

  private final WebClient webClient;

  @Value("${ncp.ocr.secret-key}")
  private String ocrSecretKey;

  @Value("${ncp.ocr.api-url}")
  private String ocrApiUrl;

  /** 파일들에서 추출한 텍스트를 합쳐서 반환 */
  public Mono<String> extractTextFromFiles(File file) {
    if (file == null) {
      return Mono.just("");
    }
    return callOcrApi(file).map(this::parseTextFromResponse);
  }

  private Mono<OcrResponse> callOcrApi(File file) {
    byte[] fileBytes;
    try {
      fileBytes = Files.readAllBytes(file.toPath());
    } catch (Exception e) {
      return Mono.error(e);
    }

    String base64Data = Base64.getEncoder().encodeToString(fileBytes);
    Map<String, Object> requestBody =
        Map.of(
            "version", "V2",
            "requestId", UUID.randomUUID().toString(),
            "timestamp", System.currentTimeMillis(),
            "lang", "ko",
            "images", List.of(Map.of("format", "png", "name", file.getName(), "data", base64Data)));

    return webClient
        .post()
        .uri(ocrApiUrl)
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-OCR-SECRET", ocrSecretKey)
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(OcrResponse.class);
  }

  private String parseTextFromResponse(OcrResponse response) {
    if (response == null || response.images() == null || response.images().isEmpty()) {
      return "";
    }

    return response.images().stream()
        .flatMap(image -> image.fields().stream())
        .map(OcrResponse.OcrFieldDto::inferText)
        .collect(Collectors.joining(" "));
  }
}
