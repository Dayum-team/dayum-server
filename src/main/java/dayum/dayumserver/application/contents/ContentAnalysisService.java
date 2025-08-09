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

  public List<ExtractedIngredientData> analyzeIngredients(String contentsUrl) {
    Path workingDir = createWorkingDirectory();

    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      StructuredTaskScope.Subtask<String> ocrTask =
          scope.fork(
              () -> {
                File downloaded = s3ClientService.downloadFile(contentsUrl, workingDir);
                List<File> frames = frameExtractorService.extractFrames(downloaded, workingDir);
                return ocrService.extractTextFromFiles(frames);
              });
      StructuredTaskScope.Subtask<String> recognizeSpeechTask =
          scope.fork(() -> ncpSpeechClient.recognize(contentsUrl).fullText());

      scope.join();
      scope.throwIfFailed();
      return extractIngredientsWithAI(ocrTask.get(), recognizeSpeechTask.get());
    } catch (Exception e) {
      throw new RuntimeException("Ingredient analysis failed", e);
    } finally {
      deleteWorkingDirectory(workingDir);
    }
  }

  private Path createWorkingDirectory() {
    try {
      Path workingDir =
          Paths.get(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
      Files.createDirectory(workingDir);
      return workingDir;
    } catch (IOException e) {
      throw new AppException(CommonExceptionCode.INTERNAL_SERVER_ERROR);
    }
  }

  private List<ExtractedIngredientData> extractIngredientsWithAI(
      String subtitleText, String speechText) {
    return parseIngredientsFromJson(clovaService.extractIngredients(subtitleText, speechText));
  }

  private void deleteWorkingDirectory(Path path) {
    try {
      Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    } catch (IOException e) {
      throw new AppException(CommonExceptionCode.INTERNAL_SERVER_ERROR);
    }
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
