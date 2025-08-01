package dayum.dayumserver.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dayum.dayumserver.common.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AppException.class)
  public ErrorResponse handleAppException(AppException exception) {
    return ErrorResponse.of(exception);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e) {

    AppExceptionCode code = CommonExceptionCode.REQUEST_BODY_MISSING;
    return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code));
  }
}
