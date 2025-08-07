package dayum.dayumserver.client.ai.speech;

import dayum.dayumserver.client.ai.speech.dto.NcpSpeechRecognizeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class NcpSpeechClient {

  @Value("${ncp.speech.base-url}")
  private String baseUrl;

  @Value("${ncp.speech.secret-key}")
  private String secretKey;

  private final RestClient restClient;

  public NcpSpeechRecognizeResponse recognize(String url) {
    var requestBody =
        new NcpSpeechRecognizeUrlRequest(
            url, "ko-KR", "sync", false, "JSON", new Diarization(false));

    try {
      return restClient
          .post()
          .uri(baseUrl + "/recognizer/url")
          .header("X-CLOVASPEECH-API-KEY", secretKey)
          .body(requestBody)
          .retrieve()
          .body(NcpSpeechRecognizeResponse.class);
    } catch (RestClientResponseException e) {
      throw new RuntimeException(
          "CLOVA Speech error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
    } catch (Exception e) {
      throw new RuntimeException("CLOVA Speech call failed", e);
    }
  }

  private record NcpSpeechRecognizeUrlRequest(
      String url,
      String language,
      String completion,
      boolean wordAlignment,
      String format,
      Diarization diarization) {}

  private record Diarization(boolean enable) {}
}
