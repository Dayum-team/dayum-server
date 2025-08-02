package dayum.dayumserver.application.common.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

  private final AppExceptionCode exceptionCode;

  public AppException(AppExceptionCode exceptionCode) {
    super(exceptionCode.getMessage());
    this.exceptionCode = exceptionCode;
  }
}
