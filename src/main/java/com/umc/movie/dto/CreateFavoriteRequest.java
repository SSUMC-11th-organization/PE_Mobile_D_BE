package com.umc.movie.dto;

import jakarta.validation.constraints.NotNull;

public record CreateFavoriteRequest(@NotNull Long movieId) {}
