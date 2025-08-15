package dayum.dayumserver.application.member.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank String accessToken,
    @NotBlank String nickname,
    String authorizationCode,
    String idToken,
    String profileImage,
    String bio) {}
