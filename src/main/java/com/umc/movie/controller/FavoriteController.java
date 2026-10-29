package com.umc.movie.controller;

import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class FavoriteController {
    private final FavoriteService favoriteService;

    @PostMapping ("/{memberId}/favorites")
    ResponseEntity <FavoriteResponse> createFavorite(
            @PathVariable Long memberId,
            @Valid @RequestBody CreateFavoriteRequest request){
        FavoriteResponse favoriteResponse = favoriteService.createFavorite(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(favoriteResponse);
    }
}
