package dayum.dayumserver.application.contents;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dayum.dayumserver.application.common.exception.AppException;
import dayum.dayumserver.application.common.exception.CommonExceptionCode;
import dayum.dayumserver.application.contents.dto.internal.ExtractedIngredientData;
import dayum.dayumserver.client.ai.chat.clova.ClovaService;
import dayum.dayumserver.client.ai.ocr.OcrService;
import dayum.dayumserver.client.ai.speech.NcpSpeechClient;
import dayum.dayumserver.client.cv.FrameExtractorService;
import dayum.dayumserver.client.s3.S3ClientService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentAnalysisService {

  private final S3ClientService s3ClientService;
  private final FrameExtractorService frameExtractorService;
  private final OcrService ocrService;
  private final NcpSpeechClient ncpSpeechClient;
  private final ClovaService clovaService;
  private final ObjectMapper objectMapper;

  public List<ExtractedIngredientData> analyzeIngredients(
      String contentsUrl, File contents, Path workingDir) {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      StructuredTaskScope.Subtask<String> ocrTask =
          scope.fork(
              () ->
                  frameExtractorService
                      .extractFrames(contents, workingDir)
                      .flatMap(ocrService::extractTextFromFiles, 5)
                      .collect(Collectors.joining(" "))
                      .block());
      StructuredTaskScope.Subtask<String> recognizeSpeechTask =
          scope.fork(() -> ncpSpeechClient.recognize(contentsUrl).fullText());

      scope.join();
      scope.throwIfFailed();
      return extractIngredientsWithAI(ocrTask.get(), recognizeSpeechTask.get());
    } catch (Exception e) {
      throw new RuntimeException("Ingredient analysis failed", e);
    }
  }

  private List<ExtractedIngredientData> extractIngredientsWithAI(
      String subtitleText, String speechText) {
    return parseIngredientsFromJson(clovaService.extractIngredients(subtitleText, speechText));
  }

  private List<ExtractedIngredientData> parseIngredientsFromJson(String jsonResponse) {
    try {
      JsonNode root = objectMapper.readTree(jsonResponse);
      JsonNode ingredientsNode = root.get("ingredients");

      return objectMapper.convertValue(
          ingredientsNode, new TypeReference<List<ExtractedIngredientData>>() {});
    } catch (Exception e) {
      log.error("Failed to parse ingredients JSON: {}", jsonResponse, e);
      return List.of();
    }
  }
}
