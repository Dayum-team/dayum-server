package dayum.dayumserver.client.cv;

import dayum.dayumserver.application.common.exception.AppException;
import dayum.dayumserver.application.common.exception.CommonExceptionCode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FrameExtractorService {

  private static final int FRAME_EXTRACTION_INTERVAL_SECONDS = 3; // 몇 초마다 프레임을 추출할지
  private static final String FRAME_FILE_PREFIX = "frame-";
  private static final String FRAME_FILE_EXTENSION = "png";

  public List<File> extractFrames(File videoFile, Path workingDir) {
    List<File> frameFiles = new ArrayList<>();

    try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile)) {
      grabber.start();

      double frameRate = grabber.getFrameRate();
      Java2DFrameConverter converter = new Java2DFrameConverter();
      Frame frame;
      int frameCount = 0;

      // 프레임 추출 간격 계산
      int frameInterval = (int) Math.round(frameRate * FRAME_EXTRACTION_INTERVAL_SECONDS);

      while ((frame = grabber.grabImage()) != null) {
        if (frameCount % frameInterval == 0) {
          BufferedImage bufferedImage = converter.convert(frame);
          if (bufferedImage != null) {
            int frameIndex = frameCount / frameInterval;
            String fileName = FRAME_FILE_PREFIX + frameIndex + "." + FRAME_FILE_EXTENSION;
            Path outputPath = workingDir.resolve(fileName);
            File outputFile = outputPath.toFile();

            ImageIO.write(bufferedImage, FRAME_FILE_EXTENSION, outputFile);
            frameFiles.add(outputFile);
          }
        }
        frameCount++;
      }

      log.info("총 {}개의 프레임 추출 완료 ({}초 간격)", frameFiles.size(), FRAME_EXTRACTION_INTERVAL_SECONDS);
      return frameFiles;

    } catch (IOException e) {
      cleanupFiles(frameFiles);
      throw new AppException(CommonExceptionCode.FRAME_EXTRACTION_FAILED);
    }
  }

  private void cleanupFiles(List<File> files) {
    files.stream().filter(file -> file != null && file.exists()).forEach(File::delete);
  }
}
