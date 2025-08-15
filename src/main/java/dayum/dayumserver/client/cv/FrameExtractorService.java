package dayum.dayumserver.client.cv;

import dayum.dayumserver.application.common.exception.AppException;
import dayum.dayumserver.application.common.exception.CommonExceptionCode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class FrameExtractorService {

  private final Java2DFrameConverter converter;

  public static final String FRAME_FILE_PREFIX = "frame-";
  public static final String FRAME_FILE_EXTENSION = "png";


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

  public Flux<File> extractFrames(File videoFile, Path workingDir) {
    return Flux.generate(() -> new FrameExtractor(videoFile, workingDir, converter), (extractor, sink) -> {
      try {
        File nextFrame = extractor.next();
        if (nextFrame != null) sink.next(nextFrame);
        else sink.complete();
      } catch (Exception e) {
        sink.error(new AppException(CommonExceptionCode.FRAME_EXTRACTION_FAILED));
      }
      return extractor;
    }, FrameExtractor::close);
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
