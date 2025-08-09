package dayum.dayumserver.domain.member;

import java.util.Arrays;

public enum Oauth2Provider {
  NAVER,
  APPLE;

  public static Oauth2Provider from(String value) {
    return Arrays.stream(values())
        .filter(p -> p.name().equalsIgnoreCase(value))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unsupported provider: " + value));
  }
}
