package com.umc.movie.service;

import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @Test
    @DisplayName("즐겨찾기 등록에 성공한다")
    void createFavoriteSuccess() {
        // given
        Long memberId = 1L;
        Long movieId = 550L;

        Member member = mock(Member.class);
        CreateFavoriteRequest request =
                new CreateFavoriteRequest(movieId);

        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(member));

        when(favoriteRepository
                .existsByMember_MemberIdAndMovieId(memberId, movieId))
                .thenReturn(false);

        when(favoriteRepository.save(any(Favorite.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        FavoriteResponse response =
                favoriteService.create(memberId, request);

        // then
        assertEquals(movieId, response.movieId());

        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    @DisplayName("이미 즐겨찾기한 영화는 중복 등록할 수 없다")
    void createFavoriteDuplicate() {
        // given
        Long memberId = 1L;
        Long movieId = 550L;

        Member member = mock(Member.class);
        CreateFavoriteRequest request =
                new CreateFavoriteRequest(movieId);

        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(member));

        when(favoriteRepository
                .existsByMember_MemberIdAndMovieId(memberId, movieId))
                .thenReturn(true);

        // when
        ApiException exception = assertThrows(
                ApiException.class,
                () -> favoriteService.create(memberId, request)
        );

        // then
        assertEquals("FAVORITE409_1", exception.getCode());

        verify(favoriteRepository, never())
                .save(any(Favorite.class));
    }
}