package com.team25.backend.domain.login.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReissueRequest(
        @NotBlank(message = "Refresh 토큰의 형식이 잘못되었습니다.")
        String refreshToken) {
}