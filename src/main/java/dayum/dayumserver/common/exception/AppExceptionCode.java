package dayum.dayumserver.common.exception;

import org.springframework.http.HttpStatus;

public interface AppExceptionCode {
  HttpStatus getStatus();

  String getCode();

  String getMessage();
}
