package com.umc.movie.service;

import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RatingService ratingService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email("umc@example.com")
                .password("password")
                .nickname("tester")
                .build();
    }

    @Test
    @DisplayName("같은 영화에 평점을 중복 등록하면 RATING409_1 예외가 발생한다")
    void duplicateRatingThrowsRating409() {

        Long memberId = 1L;
        Long movieId = 550L;

        CreateRatingRequest request =
                new CreateRatingRequest(movieId, 5, "재밌어요");

        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(member));

        when(ratingRepository.existsByMember_MemberIdAndMovieId(
                memberId,
                movieId
        )).thenReturn(true);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> ratingService.create(memberId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("RATING409_1", exception.getCode());

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    @DisplayName("남의 평점을 수정하려 하면 RATING403_1 예외가 발생한다")
    void updateOtherMembersRatingThrowsRating403() {

        Long ratingId = 10L;
        Long loginMemberId = 1L;
        Long ownerMemberId = 2L;

        Rating rating = org.mockito.Mockito.mock(Rating.class);
        Member owner = org.mockito.Mockito.mock(Member.class);

        UpdateRatingRequest request =
                new UpdateRatingRequest(4, "수정된 후기");

        when(ratingRepository.findById(ratingId))
                .thenReturn(Optional.of(rating));

        when(rating.getMember())
                .thenReturn(owner);

        when(owner.getMemberId())
                .thenReturn(ownerMemberId);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> ratingService.update(
                        ratingId,
                        loginMemberId,
                        request
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("RATING403_1", exception.getCode());

        verify(rating, never()).update(any(), any());
    }
}