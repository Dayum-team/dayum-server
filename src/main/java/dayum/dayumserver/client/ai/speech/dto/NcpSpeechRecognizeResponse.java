package dayum.dayumserver.client.ai.speech.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NcpSpeechRecognizeResponse(
    @JsonProperty("text") String fullText, @JsonProperty("confidence") double confidenceScore) {}
