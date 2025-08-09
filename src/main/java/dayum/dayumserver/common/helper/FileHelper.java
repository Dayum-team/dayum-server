package dayum.dayumserver.common.helper;

import dayum.dayumserver.application.common.exception.AppException;
import dayum.dayumserver.application.common.exception.CommonExceptionCode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;

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
}
