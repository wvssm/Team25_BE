package com.team25.backend.domain.login.service;

import com.team25.backend.domain.login.dto.response.TokenResponse;
import com.team25.backend.domain.login.entity.Refresh;
import com.team25.backend.domain.login.repository.RefreshRepository;
import com.team25.backend.domain.user.dto.response.UserResponse;
import com.team25.backend.global.util.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JWTServiceTest {
    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private RefreshRepository refreshRepository;

    @InjectMocks
    private JWTService jwtService;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("JWT서비스가 JWT 토큰을 생성하고 반환하는지 테스트")
    void generateJwtToken() {
        // given
        UserResponse userResponse = new UserResponse("user","test-uuid","ROLE_USER");

        String mockAccessToken = "mockAccessToken";
        String mockRefreshToken = "mockRefreshToken";
        when(jwtUtil.createJwt("access", userResponse.uuid(), 900000L)).thenReturn(mockAccessToken);
        when(jwtUtil.createJwt("refresh", userResponse.uuid(), 2592000000L)).thenReturn(mockRefreshToken);

        // when
        TokenResponse tokenResponse = jwtService.generateJwtToken(userResponse);

        // then
        assertEquals(mockAccessToken, tokenResponse.accessToken());
        assertEquals(mockRefreshToken, tokenResponse.refreshToken());
        assertEquals(900000L, tokenResponse.expiresIn());
        assertEquals(2592000000L, tokenResponse.refreshTokenExpiresIn());
        verify(refreshRepository, times(1)).save(any(Refresh.class));
    }

    @Test
    @DisplayName("JWT서비스가 refresh 토큰을 DB에 저장하는지 테스트")
    void addRefresh() {
        // given
        String uuid = "test-uuid";
        String refreshToken = "mockRefreshToken";
        Long expiryMs = 120000L;

        // when
        jwtService.addRefresh(uuid, refreshToken, expiryMs);

        // then
        verify(refreshRepository, times(1)).save(any(Refresh.class));
    }
}