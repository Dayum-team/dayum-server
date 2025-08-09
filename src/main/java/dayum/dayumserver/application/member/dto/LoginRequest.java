package dayum.dayumserver.application.member.dto;

public record LoginRequest(String accessToken, String authorizationCode, String idToken) {}
