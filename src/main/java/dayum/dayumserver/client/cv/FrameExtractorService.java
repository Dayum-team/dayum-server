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

  private static final int FRAME_EXTRACTION_INTERVAL_SECONDS = 3;
  private static final String FRAME_FILE_PREFIX = "frame-";
  private static final String FRAME_FILE_EXTENSION = "png";

  private final Java2DFrameConverter converter = new Java2DFrameConverter();

  public File extractThumbnail(File videoFile, Path workingDir) {
    try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile)) {
      grabber.setOption("skip_frame", "nokey");
      grabber.start();

      final int MAX_KEYFRAMES = 40;
      final int MIN_MEAN_LUMA = 20;
      int seen = 0;
      Frame frame;

      while ((frame = grabber.grabImage()) != null && seen < MAX_KEYFRAMES) {
        BufferedImage bufferedImage = converter.convert(frame);
        if (bufferedImage == null) { seen++; continue; }

        double mean = meanLuma(centerCrop(bufferedImage));
        if (mean >= MIN_MEAN_LUMA) {
            String fileName = STR."\{FRAME_FILE_PREFIX}\{frame.timestamp}.\{FRAME_FILE_EXTENSION}";
            File outputFile = workingDir.resolve(fileName).toFile();
            ImageIO.write(bufferedImage, FRAME_FILE_EXTENSION, outputFile);
            return outputFile;
          }
        seen++;
      }
      throw new IOException("No suitable frame found.");
    } catch (IOException e) {
      throw new AppException(CommonExceptionCode.FRAME_EXTRACTION_FAILED);
    }
  }

  public List<File> extractFrames(File videoFile, Path workingDir) {
    List<File> frameFiles = new ArrayList<>();

    try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile)) {
      grabber.start();

      double frameRate = grabber.getFrameRate();
      Frame frame;
      int frameCount = 0;

      int frameInterval = (int) Math.round(frameRate * FRAME_EXTRACTION_INTERVAL_SECONDS);

      while ((frame = grabber.grabImage()) != null) {
        if (frameCount % frameInterval == 0) {
          BufferedImage bufferedImage = converter.convert(frame);
          if (bufferedImage != null) {
            int frameIndex = frameCount / frameInterval;
            String fileName = STR."\{FRAME_FILE_PREFIX}\{frameIndex}.\{FRAME_FILE_EXTENSION}";
            File outputFile = workingDir.resolve(fileName).toFile();
            ImageIO.write(bufferedImage, FRAME_FILE_EXTENSION, outputFile);
            frameFiles.add(outputFile);
          }
        }
        frameCount++;
      }
      return frameFiles;

    } catch (IOException e) {
      cleanupFiles(frameFiles);
      throw new AppException(CommonExceptionCode.FRAME_EXTRACTION_FAILED);
    }
  }

  private void cleanupFiles(List<File> files) {
    files.stream().filter(file -> file != null && file.exists()).forEach(File::delete);
  }

  private static BufferedImage centerCrop(BufferedImage src) {
    double ratio = 0.6;
    int w = (int) Math.round(src.getWidth() * ratio);
    int h = (int) Math.round(src.getHeight() * ratio);
    int x = (src.getWidth() - w) / 2;
    int y = (src.getHeight() - h) / 2;
    return src.getSubimage(x, y, w, h);
  }

  private static double meanLuma(BufferedImage img) {
    long sum = 0;
    int w = img.getWidth(), h = img.getHeight();
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int rgb = img.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int yv = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
        sum += yv;
      }
    }
    return (double) sum / (w * h);
  }

}
