package dayum.dayumserver.application.common.response;

import dayum.dayumserver.application.common.exception.AppException;
import dayum.dayumserver.application.common.exception.AppExceptionCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {
  private final boolean success = false;
  private final String errorCode;
  private final String errorMessage;

  public static ErrorResponse of(AppException appException) {
    return of(appException.getExceptionCode());
  }

  public static ErrorResponse of(AppExceptionCode exceptionCode) {
    return new ErrorResponse(exceptionCode.getCode(), exceptionCode.getMessage());
  }
}
