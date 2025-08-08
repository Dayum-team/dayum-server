package dayum.dayumserver.client.ocr;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OcrService {

  private static final int MAX_CONCURRENT_REQUESTS = 5;

  private final RestClient restClient;

  private final ExecutorService ocrThreadPool =
      Executors.newFixedThreadPool(MAX_CONCURRENT_REQUESTS);

  @Value("${ncp.ocr.secret-key}")
  private String ocrSecretKey;

  @Value("${ncp.ocr.api-url}")
  private String ocrApiUrl;

  @PreDestroy
  public void shutdown() {
    ocrThreadPool.shutdown();
  }

  /** 파일들에서 추출한 텍스트를 합쳐서 반환 */
  public String extractTextFromFiles(List<File> files) {
    if (files == null || files.isEmpty()) {
      return "";
    }

    List<CompletableFuture<String>> futures =
        files.stream()
            .map(
                file ->
                    CompletableFuture.supplyAsync(() -> extractTextFromFile(file), ocrThreadPool))
            .collect(Collectors.toList());

    return futures.stream()
        .map(CompletableFuture::join)
        .filter(text -> !text.isEmpty())
        .collect(Collectors.joining(" "))
        .trim();
  }

  private String extractTextFromFile(File file) {
    try {
      OcrResponse response = callOcrApi(file);
      return parseTextFromResponse(response);
    } catch (Exception e) {
      log.info("OCR API call failed: {}", e.getMessage());
      return "";
    }
  }

  private OcrResponse callOcrApi(File file) throws IOException {
    byte[] fileBytes = Files.readAllBytes(file.toPath());
    String base64Data = Base64.getEncoder().encodeToString(fileBytes);

    Map<String, Object> requestBody =
        Map.of(
            "version", "V2",
            "requestId", UUID.randomUUID().toString(),
            "timestamp", System.currentTimeMillis(),
            "lang", "ko",
            "images", List.of(Map.of("format", "png", "name", file.getName(), "data", base64Data)));

    return restClient
        .post()
        .uri(ocrApiUrl)
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-OCR-SECRET", ocrSecretKey)
        .body(requestBody)
        .retrieve()
        .body(OcrResponse.class);
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
