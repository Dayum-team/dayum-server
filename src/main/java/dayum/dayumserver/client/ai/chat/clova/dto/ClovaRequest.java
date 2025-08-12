package dayum.dayumserver.client.ai.chat.clova.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = TextContent.class, name = "text"),
    @JsonSubTypes.Type(value = ImageContent.class, name = "image_url")
  })
  public interface Content {}

  public record TextContent(String type, String text) implements Content {}

  public record ImageContent(String type, @JsonProperty("imageUrl") ImageUrl imageUrl)
      implements Content {}

  public record ImageUrl(String url) {}

  // 텍스트만 있는 경우
  public static ClovaRequest of(String systemMessage, String userMessage) {
    return ClovaRequest.builder()
        .messages(
            List.of(
                new Message("system", List.of(new TextContent("text", systemMessage))),
                new Message("user", List.of(new TextContent("text", userMessage)))))
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

  // 이미지와 텍스트를 함께 보내는 경우
  public static ClovaRequest ofWithImage(
      String systemMessage, String imageUrl, String userMessage) {
    return ClovaRequest.builder()
        .messages(
            List.of(
                new Message("system", List.of(new TextContent("text", systemMessage))),
                new Message(
                    "user",
                    List.of(
                        new ImageContent("image_url", new ImageUrl(imageUrl)),
                        new TextContent("text", userMessage)))))
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
