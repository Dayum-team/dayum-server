package dayum.dayumserver.application.common.response;

import java.util.List;

public record PageResponse<T>(List<T> items, PageInfo pageInfo) {

  public record PageInfo(String nextCursor, boolean isLastPage) {}
}
