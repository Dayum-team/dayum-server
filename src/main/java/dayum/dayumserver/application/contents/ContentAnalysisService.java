package dayum.dayumserver.application.contents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dayum.dayumserver.application.common.exception.AppException;
import dayum.dayumserver.application.common.exception.CommonExceptionCode;
import dayum.dayumserver.client.cv.FrameExtractorService;
import dayum.dayumserver.client.ocr.OcrService;
import dayum.dayumserver.client.s3.S3ClientService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentAnalysisService {

  private final S3ClientService s3ClientService;
  private final FrameExtractorService frameExtractorService;
  private final OcrService ocrService;

  public Map<String, Object> analyzeIngredients(String contentsUrl) {
    String uniqueId = UUID.randomUUID().toString();
    Path workingDir = createWorkingDirectory(uniqueId);

    try {
      File downloadedFile = s3ClientService.downloadFile(contentsUrl, workingDir);
      List<File> frameFiles = frameExtractorService.extractFrames(downloadedFile, workingDir);

      Map<String, String> ocrTexts = ocrService.extractTextFromFiles(frameFiles);

      return extractIngredientsWithAI(ocrTexts);

    } finally {
      deleteWorkingDirectory(workingDir);
    }
  }

  private Path createWorkingDirectory(String uniqueId) {
    try {
      Path workingDir = Paths.get(System.getProperty("java.io.tmpdir"), uniqueId);
      Files.createDirectory(workingDir);
      return workingDir;
    } catch (IOException e) {
      throw new AppException(CommonExceptionCode.INTERNAL_SERVER_ERROR);
    }
  }

  private Map<String, Object> extractIngredientsWithAI(Map<String, String> ocrTexts) {
    // Clova Studio 로직 추가 예정
    return Map.of();
  }

  private void deleteWorkingDirectory(Path path) {
    try {
      Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    } catch (IOException e) {
      throw new AppException(CommonExceptionCode.INTERNAL_SERVER_ERROR);
    }
  }
}
