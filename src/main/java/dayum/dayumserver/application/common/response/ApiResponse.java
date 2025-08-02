package dayum.dayumserver.application.common.response;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
  private final boolean success = true;
  private final T data;

  private ApiResponse(T data) {
    this.data = data;
  }

  public static <T> ApiResponse<T> of(T data) {
    return new ApiResponse<>(data);
  }
}
