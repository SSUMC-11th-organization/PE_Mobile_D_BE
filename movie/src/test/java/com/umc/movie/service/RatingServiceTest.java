// src/test/java/com/umc/movie/service/RatingServiceTest.java
package com.umc.movie.service;

import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // @Mock이 붙은 가짜 객체를 만들어주는 확장입니다
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository; // 진짜 DB 대신 들어가는 가짜 Repository

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RatingService ratingService; // 위의 가짜 두 개를 주입받은 진짜 Service

    private Member memberWithId(Long memberId, String nickname) {
        Member member = Member.builder()
                .email(nickname + "@umc.com")
                .password("암호화된비밀번호")
                .nickname(nickname)
                .build();
        // memberId는 DB가 만들어주는 값이라 테스트에서는 직접 넣어줍니다
        ReflectionTestUtils.setField(member, "memberId", memberId);
        return member;
    }

    @Test
    @DisplayName("같은 영화에 평점을 중복 등록하면 RATING409_1 예외를 던지고 저장하지 않는다")
    void createRatingWhenDuplicated() {
        // given: 같은 회원이 같은 영화에 이미 평점을 남겨 둔 상황
        Member member = memberWithId(1L, "영화조아");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(true);

        // when & then: 409와 RATING409_1로 거절하고, 저장은 시도조차 하지 않는다
        assertThatThrownBy(() -> ratingService.create(1L, new CreateRatingRequest(550L, 5, "다시 봐도 명작")))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(e.getCode()).isEqualTo("RATING409_1");
                });

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    @DisplayName("다른 회원의 평점을 수정하려 하면 RATING403_1 예외를 던지고 저장하지 않는다")
    void updateRatingOfOtherMember() {
        // given: 평점의 주인은 2번 회원인데, 1번 회원이 수정하려는 상황
        Member owner = memberWithId(2L, "주인");
        Rating rating = Rating.builder().member(owner).movieId(550L).score(3).comment("보통").build();
        given(ratingRepository.findById(10L)).willReturn(Optional.of(rating));

        // when & then: 평점은 존재하지만 내 것이 아니므로 403과 RATING403_1로 거절한다
        assertThatThrownBy(() -> ratingService.update(1L, 10L, new UpdateRatingRequest(5, "내가 고쳐볼래")))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(e.getCode()).isEqualTo("RATING403_1");
                });

        // 거절됐으니 평점 내용은 그대로이고 저장도 일어나지 않는다
        assertThat(rating.getScore()).isEqualTo(3);
        verify(ratingRepository, never()).save(any(Rating.class));
    }
}
