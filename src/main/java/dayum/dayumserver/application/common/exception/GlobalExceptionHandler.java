package dayum.dayumserver.application.common.exception;

import dayum.dayumserver.application.common.response.ErrorResponse;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AppException.class)
  public ResponseEntity<ErrorResponse> handleAppException(AppException exception) {
    log.error(exception.getMessage(), exception);
    return ResponseEntity.status(exception.getExceptionCode().getStatus())
        .body(ErrorResponse.of(exception));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e) {
    AppExceptionCode code = CommonExceptionCode.REQUEST_BODY_MISSING;
    return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
    log.error("Internal server error!", e);
    Sentry.captureException(e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }
}
