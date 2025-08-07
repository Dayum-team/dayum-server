package dayum.dayumserver.infrastructure.repository;

import dayum.dayumserver.application.common.exception.AppException;
import dayum.dayumserver.application.common.exception.AppExceptionCode;
import dayum.dayumserver.application.common.exception.CommonExceptionCode;
import dayum.dayumserver.domain.member.Member;
import dayum.dayumserver.domain.member.MemberRepository;
import dayum.dayumserver.infrastructure.repository.jpa.repository.MemberJpaRepository;
import dayum.dayumserver.infrastructure.repository.mapper.MemberMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryJpaAdaptor implements MemberRepository {

  private final MemberJpaRepository memberJpaRepository;
  private final MemberMapper memberMapper;

  @Override
  public Member fetchBy(Long memberId) {
    return memberJpaRepository
        .findById(memberId)
        .map(memberMapper::mapToDomainEntity)
        .orElseThrow(() -> new AppException(CommonExceptionCode.BAD_REQUEST));
  }
}
