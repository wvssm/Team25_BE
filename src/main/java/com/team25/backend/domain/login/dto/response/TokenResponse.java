package com.team25.backend.domain.login.dto.response;

public record TokenResponse (
        String accessToken,
        Long expiresIn,
        String refreshToken,
        Long refreshTokenExpiresIn
) {
}
