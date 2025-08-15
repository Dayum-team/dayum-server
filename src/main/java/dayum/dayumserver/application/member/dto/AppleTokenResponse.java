package dayum.dayumserver.application.member.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AppleTokenResponse(
    String accessToken, String expiresIn, String idToken, String refreshToken, String tokenType) {}
