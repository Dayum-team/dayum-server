package dayum.dayumserver.application.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonExceptionCode implements AppExceptionCode {
  REQUEST_BODY_MISSING(HttpStatus.BAD_REQUEST, "C-REQUEST-001", "필수 요청 본문(Request Body)이 누락되었습니다."),

  S3_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_001", "파일 다운로드 중 서버 내부 오류가 발생했습니다."),

  FRAME_EXTRACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CV_001", "영상 프레임 추출 중 서버 내부 오류가 발생했습니다."),

  FILE_ENCODING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FL_001", "파일 인코딩 중 서버 내부 오류가 발생했습니다."),

  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S_001", "서버 내부 오류가 발생했습니다.");


  private final HttpStatus status;
  private final String code;
  private final String message;
}
