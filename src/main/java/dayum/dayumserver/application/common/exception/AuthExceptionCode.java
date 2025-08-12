package dayum.dayumserver.application.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthExceptionCode implements AppExceptionCode {
  UN_AUTHORIZATION(HttpStatus.UNAUTHORIZED, "A_001", "사용자 인증이 필요한 요청입니다."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "A_002", "허용되지 않은 요청입니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
