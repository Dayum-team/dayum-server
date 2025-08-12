package dayum.dayumserver.application.member.dto;

import dayum.dayumserver.domain.member.Member;

public record MemberProfileResponse(String nickname, String profileImage, String bio) {
  public static MemberProfileResponse from(Member member) {
    return new MemberProfileResponse(member.nickname(), member.profileImage(), member.bio());
  }
}
