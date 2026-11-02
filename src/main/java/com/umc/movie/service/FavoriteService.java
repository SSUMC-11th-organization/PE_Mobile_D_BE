package com.umc.movie.service;

import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public FavoriteResponse createFavorite(Long memberId, CreateFavoriteRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다. memberId=" + memberId));

        if (favoriteRepository.existsByMemberMemberIdAndMovieId(memberId, request.movieId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "이미 즐겨찾기에 추가된 영화입니다.");
        }

        Favorite saved = favoriteRepository.save(new Favorite(member, request.movieId()));
        return FavoriteResponse.from(saved);
    }
}