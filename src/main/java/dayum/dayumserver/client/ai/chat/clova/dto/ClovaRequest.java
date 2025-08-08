package dayum.dayumserver.client.ai.chat.clova.dto;

import java.util.List;

import dayum.dayumserver.client.ai.chat.clova.ClovaStudioProperties;
import lombok.Builder;

@Builder
public record ClovaRequest(
    List<Message> messages,
    Double topP,
    Integer topK,
    Integer maxTokens,
    Double temperature,
    Double repetitionPenalty,
    List<String> stop,
    long seed,
    Boolean includeAiFilters) {

  public record Message(String role, List<Content> content) {}

  public record Content(String type, String text) {}

  public static ClovaRequest of(String systemMessage, String userMessage) {
    return ClovaRequest.builder()
        .messages(
            List.of(
                new Message("system", List.of(new Content("text", systemMessage))),
                new Message("user", List.of(new Content("text", userMessage)))))
        .topP(ClovaStudioProperties.ModelConfig.TOP_P)
        .topK(ClovaStudioProperties.ModelConfig.TOP_K)
        .maxTokens(ClovaStudioProperties.ModelConfig.MAX_TOKENS)
        .temperature(ClovaStudioProperties.ModelConfig.TEMPERATURE)
        .repetitionPenalty(ClovaStudioProperties.ModelConfig.REPETITION_PENALTY)
        .stop(List.of())
        .seed(ClovaStudioProperties.ModelConfig.SEED)
        .includeAiFilters(ClovaStudioProperties.ModelConfig.INCLUDE_AI_FILTERS)
        .build();
  }
}
