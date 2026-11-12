package com.umc.movie.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateNicknameRequest(

        @NotBlank(message = "닉네임을 입력해주세요.")
        String nickname
) {
}
