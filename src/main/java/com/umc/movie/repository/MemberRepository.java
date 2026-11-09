// src/main/java/com/umc/movie/repository/MemberRepository.java
package com.umc.movie.repository;

import java.util.Optional;
import com.umc.movie.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByNickname(String nickname); // 닉네임이 존재하는가
    boolean existsByEmail(String email); // email이 존재하는가
    // 이메일로 회원을 찾아야하므로, 회원 빼내오는 메서드 추가
    Optional<Member> findByEmail(String email);

    // "이 닉네임을 쓰는 회원이 있는가? 단, 나 자신은 빼고" 를 한 번에 확인합니다
    boolean existsByNicknameAndMemberIdNot(String nickname, Long memberId);
}
