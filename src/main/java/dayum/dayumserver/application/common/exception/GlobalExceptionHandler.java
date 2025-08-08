package dayum.dayumserver.application.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dayum.dayumserver.application.common.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AppException.class)
  public ResponseEntity<ErrorResponse> handleAppException(AppException exception) {
    return ResponseEntity.status(exception.getExceptionCode().getStatus())
        .body(ErrorResponse.of(exception));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e) {

    AppExceptionCode code = CommonExceptionCode.REQUEST_BODY_MISSING;
    return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code));
  }
}
