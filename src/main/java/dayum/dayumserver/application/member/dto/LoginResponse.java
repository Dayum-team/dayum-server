package dayum.dayumserver.application.member.dto;

public record LoginResponse(
    String accessToken, String refreshToken, String nickname, String bio, String profileImage) {}
