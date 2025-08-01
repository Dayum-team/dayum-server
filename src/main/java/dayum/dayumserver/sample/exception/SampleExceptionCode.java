package dayum.dayumserver.sample.exception;

import org.springframework.http.HttpStatus;

import dayum.dayumserver.common.exception.AppExceptionCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SampleExceptionCode implements AppExceptionCode {
  ;

  private final HttpStatus status;
  private final String code;
  private final String message;
}
