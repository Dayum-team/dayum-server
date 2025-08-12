package dayum.dayumserver.common.helper;

import dayum.dayumserver.application.common.exception.AppException;
import dayum.dayumserver.application.common.exception.CommonExceptionCode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

public class FileHelper {

  public static Path createWorkingDirectory() {
    try {
      Path workingDir =
          Paths.get(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
      Files.createDirectory(workingDir);
      return workingDir;
    } catch (IOException e) {
      throw new AppException(CommonExceptionCode.INTERNAL_SERVER_ERROR);
    }
  }

  public static void deleteWorkingDirectory(Path path) {
    try {
      Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    } catch (IOException e) {
      throw new AppException(CommonExceptionCode.INTERNAL_SERVER_ERROR);
    }
  }

  public static void writeJpeg(BufferedImage image, File out, float q) throws IOException {
    ImageWriter w = ImageIO.getImageWritersByFormatName("jpg").next();
    ImageWriteParam p = w.getDefaultWriteParam();
    p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    p.setCompressionQuality(q);
    try (FileImageOutputStream fos = new FileImageOutputStream(out)) {
      w.setOutput(fos);
      w.write(null, new IIOImage(image, null, null), p);
    } finally {
      w.dispose();
    }
  }
}
