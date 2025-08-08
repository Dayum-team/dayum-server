package dayum.dayumserver.domain.member;

public interface MemberRepository {
  Member fetchBy(long memberId);
}
