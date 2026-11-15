// src/main/java/com/umc/movie/dto/LoginResponse.java
// 응답에 사용할 dto
package com.umc.movie.dto;

public record LoginResponse(
        String nickname,
        String accessToken
) {
}