package dayum.dayumserver.client.ocr;

import dayum.dayumserver.client.s3.NcpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OcrService {

  private static final int MAX_CONCURRENT_REQUESTS = 5;

  private final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_REQUESTS);

  private final NcpProperties ncpProperties;
  private final RestTemplate restTemplate;

  /** 파일별 OCR 결과를 Map으로 반환 */
  public Map<String, String> extractTextFromFiles(List<File> files) {
    if (files == null || files.isEmpty()) {
      return Map.of();
    }

    List<CompletableFuture<Map.Entry<String, String>>> futures =
        files.stream()
            .map(
                file ->
                    CompletableFuture.supplyAsync(
                        () -> {
                          String text = extractTextFromFile(file);
                          return Map.entry(file.getName(), text);
                        }))
            .collect(Collectors.toList());

    return futures.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /** Map 결과를 이어진 텍스트로 변환 */
  public String combineTexts(Map<String, String> textMap) {
    return textMap.values().stream()
        .filter(text -> !text.isEmpty())
        .collect(Collectors.joining(" "))
        .trim();
  }

  private String extractTextFromFile(File file) {
    try {
      semaphore.acquire();
      try {
        OcrResponse response = callOcrApi(file);
        return parseTextFromResponse(response);
      } finally {
        semaphore.release();
      }
    } catch (Exception e) {
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

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-OCR-SECRET", ncpProperties.getOcrSecretKey());

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
    return restTemplate.postForObject(ncpProperties.getOcrApiUrl(), request, OcrResponse.class);
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
