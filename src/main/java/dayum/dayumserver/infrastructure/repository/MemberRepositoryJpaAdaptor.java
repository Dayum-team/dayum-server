package dayum.dayumserver.infrastructure.repository;

import dayum.dayumserver.domain.member.Member;
import dayum.dayumserver.domain.member.MemberRepository;
import dayum.dayumserver.domain.member.Oauth2Provider;
import dayum.dayumserver.infrastructure.repository.jpa.entity.MemberJpaEntity;
import dayum.dayumserver.infrastructure.repository.jpa.repository.MemberJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryJpaAdaptor implements MemberRepository {

  private final MemberJpaRepository memberJpaRepository;

  @Override
  public boolean existsByNickname(String nickname) {
    return memberJpaRepository.existsByNickname(nickname);
  }

  @Override
  public Member save(Member member) {
    MemberJpaEntity entity = toEntity(member);
    MemberJpaEntity saved = memberJpaRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  public Optional<Member> findByEmailAndProvider(String email, Oauth2Provider provider) {
    return memberJpaRepository.findByEmailAndOauth2Provider(email, provider).map(this::toDomain);
  }

  @Override
  public Optional<Member> findById(Long id) {
    return memberJpaRepository.findById(id).map(this::toDomain);
  }

  private Member toDomain(MemberJpaEntity entity) {
    return new Member(
        entity.getId(),
        entity.getEmail(),
        entity.getName(),
        entity.getNickname(),
        entity.getProfileImage(),
        entity.getOauth2Provider(),
        entity.getBio(),
        entity.isDeleted(),
        entity.getDeletedAt());
  }

  private MemberJpaEntity toEntity(Member member) {
    return MemberJpaEntity.builder()
        .id(member.id())
        .email(member.email())
        .name(member.name())
        .nickname(member.nickname())
        .profileImage(member.profileImage())
        .oauth2Provider(member.oauth2Provider())
        .bio(member.bio())
        .deleted(member.deleted())
        .deletedAt(member.deletedAt())
        .build();
  }
}
