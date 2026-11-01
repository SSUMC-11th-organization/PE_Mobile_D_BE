package com.umc.movie.controller;

import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class RatingController {

    private final RatingService ratingService;

    @GetMapping("/{memberId}/ratings")
    public List<RatingResponse> getMemberRatings(@PathVariable Long memberId) {
        return ratingService.getRatingsByMember(memberId);
    }

    @PostMapping("/{memberId}/ratings")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> createRating(
            @PathVariable Long memberId,
            @Valid @RequestBody CreateRatingRequest request
    ) {
        Long ratingId = ratingService.create(memberId, request);
        return ApiResponse.created("평점이 등록되었습니다.", ratingId);
    }

    @PatchMapping("/{memberId}/ratings/{ratingId}")
    public ApiResponse<Void> updateRating(
            @PathVariable Long memberId,
            @PathVariable Long ratingId,
            @Valid @RequestBody UpdateRatingRequest request
    ) {
        ratingService.update(ratingId, memberId, request);
        return ApiResponse.ok("평점이 수정되었습니다.", null);
    }

    @DeleteMapping("/{memberId}/ratings/{ratingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRating(
            @PathVariable Long memberId,
            @PathVariable Long ratingId
    ) {
        ratingService.delete(ratingId, memberId);
    }
}