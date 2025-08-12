package dayum.dayumserver.client.ai.chat.clova;

import dayum.dayumserver.client.ai.chat.clova.dto.ClovaRequest;
import dayum.dayumserver.client.ai.chat.clova.dto.ClovaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClovaService {

  private final ClovaStudioProperties properties;
  private final RestClient restClient;

  public String extractIngredients(String subtitleText, String speechText) {
    String userPrompt = buildUserPrompt(subtitleText, speechText);
    return chatCompletion(ClovaStudioProperties.PromptConfig.INGREDIENT_EXTRACTION, userPrompt);
  }

  public String analyzeFoodImage(String imageUrl) {
    return chatCompletionWithImage(
        ClovaStudioProperties.PromptConfig.FOOD_IMAGE_ANALYSIS,
        imageUrl,
        ClovaStudioProperties.PromptConfig.FOOD_IMAGE_USER_PROMPT);
  }

  private String chatCompletion(String systemMessage, String userMessage) {
    log.info("User Message: {}", userMessage);
    ClovaRequest request = ClovaRequest.of(systemMessage, userMessage);
    return sendRequestAndExtractJson(request);
  }

  private String chatCompletionWithImage(
      String systemMessage, String imageUrl, String userMessage) {
    log.info("Analyzing image with message: {}", userMessage);
    ClovaRequest request = ClovaRequest.ofImage(systemMessage, imageUrl, userMessage);
    return sendRequestAndExtractJson(request);
  }

  // 공통 요청 처리 로직
  private String sendRequestAndExtractJson(ClovaRequest request) {
    ClovaResponse response =
        restClient
            .post()
            .uri(properties.getBaseUrl())
            .header("Authorization", "Bearer " + properties.getApiKey())
            .header("X-NCP-CLOVASTUDIO-REQUEST-ID", UUID.randomUUID().toString())
            .body(request)
            .retrieve()
            .body(ClovaResponse.class);

    if (response != null && response.result() != null && response.result().message() != null) {
      String rawContent = response.result().message().content();
      log.info("Clova Studio response: {}", rawContent);
      return extractJsonContent(rawContent);
    }
    throw new RuntimeException("Clova Studio chat completion failed");
  }

  private String extractJsonContent(String rawContent) {
    if (rawContent == null || rawContent.isBlank()) {
      return "{}";
    }

    int firstBraceIndex = rawContent.indexOf('{');
    int lastBraceIndex = rawContent.lastIndexOf('}');

    if (firstBraceIndex != -1 && lastBraceIndex != -1 && lastBraceIndex > firstBraceIndex) {
      return rawContent.substring(firstBraceIndex, lastBraceIndex + 1).trim();
    }

    log.warn("Failed to find valid JSON object in the response: {}", rawContent);
    return "{}";
  }

  private String buildUserPrompt(String ocrText, String speechText) {
    StringBuilder combined = new StringBuilder();

    if (ocrText != null && !ocrText.trim().isEmpty()) {
      combined.append("이미지에서 추출된 텍스트:\n").append(ocrText.trim()).append("\n\n");
    }
    if (speechText != null && !speechText.trim().isEmpty()) {
      combined.append("음성에서 추출된 텍스트:\n").append(speechText.trim());
    }

    return combined.toString();
  }
}
