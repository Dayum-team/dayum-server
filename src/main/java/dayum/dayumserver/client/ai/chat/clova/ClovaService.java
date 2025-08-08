package dayum.dayumserver.client.ai.chat.clova;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClovaService {

  private final ClovaStudioProperties properties;
  private final RestClient restClient;

  public String extractIngredients(String ocrText, String speechText) {
    String combinedText = buildCombinedText(ocrText, speechText);
    return chatCompletion(ClovaStudioProperties.PromptConfig.INGREDIENT_EXTRACTION, combinedText);
  }

  public String chatCompletion(String systemMessage, String userMessage) {
    var requestBody = buildRequestBody(systemMessage, userMessage);

    try {
      Map<String, Object> response =
          restClient
              .post()
              .uri(properties.getBaseUrl())
              .header("Authorization", "Bearer " + properties.getApiKey())
              .header("X-NCP-CLOVASTUDIO-REQUEST-ID", UUID.randomUUID().toString())
              .header("Content-Type", "application/json")
              .header("Accept", "application/json")
              .body(requestBody)
              .retrieve()
              .body(Map.class);

      return extractContentFromResponse(response);

    } catch (RestClientResponseException e) {
      throw new RuntimeException(
          "CLOVA Studio error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
    } catch (Exception e) {
      throw new RuntimeException("CLOVA Studio call failed", e);
    }
  }

  private String buildCombinedText(String ocrText, String speechText) {
    StringBuilder combined = new StringBuilder();

    if (ocrText != null && !ocrText.trim().isEmpty()) {
      combined.append("이미지에서 추출된 텍스트:\n").append(ocrText.trim()).append("\n\n");
    }
    if (speechText != null && !speechText.trim().isEmpty()) {
      combined.append("음성에서 추출된 텍스트:\n").append(speechText.trim());
    }

    return combined.toString();
  }

  private Map<String, Object> buildRequestBody(String systemMessage, String userMessage) {
    return Map.of(
        "messages",
        List.of(
            Map.of(
                "role",
                "system",
                "content",
                List.of(Map.of("type", "text", "text", systemMessage))),
            Map.of(
                "role", "user", "content", List.of(Map.of("type", "text", "text", userMessage)))),
        "topP",
        ClovaStudioProperties.ModelConfig.TOP_P,
        "topK",
        ClovaStudioProperties.ModelConfig.TOP_K,
        "maxTokens",
        ClovaStudioProperties.ModelConfig.MAX_TOKENS,
        "temperature",
        ClovaStudioProperties.ModelConfig.TEMPERATURE,
        "repetitionPenalty",
        ClovaStudioProperties.ModelConfig.REPETITION_PENALTY,
        "stop",
        List.of(),
        "seed",
        ClovaStudioProperties.ModelConfig.SEED,
        "includeAiFilters",
        ClovaStudioProperties.ModelConfig.INCLUDE_AI_FILTERS);
  }

  private String extractContentFromResponse(Map<String, Object> response) {
    Map<String, Object> result = (Map<String, Object>) response.get("result");
    Map<String, Object> message = (Map<String, Object>) result.get("message");
    return (String) message.get("content");
  }
}
