package dayum.dayumserver.application.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonExceptionCode implements AppExceptionCode {
  REQUEST_BODY_MISSING(HttpStatus.BAD_REQUEST, "C-REQUEST-001", "필수 요청 본문(Request Body)이 누락되었습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
