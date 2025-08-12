package dayum.dayumserver.application.member.dto;


public record RegisterRequest(
    String accessToken,
    String nickname,
    String authorizationCode,
    String idToken,
    String profileImage,
    String bio) {}
