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

  private static BufferedImage centerCrop(BufferedImage image) {
    double ratio = 0.6;
    int maxWidth = (int) Math.round(image.getWidth() * ratio);
    int maxHeight = (int) Math.round(image.getHeight() * ratio);
    int minWidth = (image.getWidth() - maxWidth) / 2;
    int minHeight = (image.getHeight() - maxHeight) / 2;
    return image.getSubimage(minWidth, minHeight, maxWidth, maxHeight);
  }

  private static double meanLuma(BufferedImage image) {
    long sum = 0;
    for (int yPoint = 0; yPoint < image.getHeight(); yPoint++) {
      for (int xPoint = 0; xPoint < image.getWidth(); xPoint++) {
        int rgb = image.getRGB(xPoint, yPoint);
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        int yv = (int) Math.round(0.299 * red + 0.587 * green + 0.114 * blue);
        sum += yv;
      }
    }
    return (double) sum / (image.getWidth() * image.getHeight());
  }
}
