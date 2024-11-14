package com.team25.backend.domain.login.service;

import com.team25.backend.domain.login.dto.response.TokenResponse;
import com.team25.backend.domain.login.repository.RefreshRepository;
import com.team25.backend.domain.user.dto.response.UserResponse;
import com.team25.backend.global.exception.CustomException;
import com.team25.backend.global.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.team25.backend.global.exception.ErrorCode.NOT_EXISTED_REFRESH_TOKEN;
import static com.team25.backend.global.exception.ErrorCode.NOT_REFRESH_TOKEN;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReissueServiceTest {
    @Mock
    private RefreshRepository refreshRepository;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private ReissueService reissueService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("만료된 토큰일때 예외 발생")
    void validateRefreshToke1() {
        // given
        String expiredRefreshToken = "invalidToken";
        when(jwtUtil.isExpired(expiredRefreshToken)).thenThrow(new ExpiredJwtException(null, null, "토큰이 만료되었습니다."));

        // when & then
        assertThatThrownBy( () ->
           reissueService.validateRefreshToken(expiredRefreshToken))
                .isInstanceOf(ExpiredJwtException.class)
                .hasMessage("토큰이 만료되었습니다.");
    }

    @Test
    @DisplayName("유효하지 않은 토큰 카테고리일 때 예외 발생")
    void validateRefreshToken2() {
        // given
        String invalidRefreshToken = "invalidToken";
        when(jwtUtil.getCategory(invalidRefreshToken)).thenReturn("access");

        // when & then
        assertThatThrownBy( () ->
                reissueService.validateRefreshToken(invalidRefreshToken))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_REFRESH_TOKEN.getMessage());
    }

    @Test
    @DisplayName("유효한 토큰 카테고리일 때 예외 발생하지 않음")
    void validateRefreshToken3() {
        // given
        String validRefreshToken = "validToken";
        when(jwtUtil.getCategory(validRefreshToken)).thenReturn("refresh");
        when(refreshRepository.existsByRefresh(validRefreshToken)).thenReturn(true);

        // when & then
        assertDoesNotThrow(() -> reissueService.validateRefreshToken(validRefreshToken));
    }

    @Test
    @DisplayName("존재하지 않는 리프레시 토큰일 때 예외 발생")
    void validateRefreshToken4() {
        // given
        String nonExistentToken = "nonExistentToken";
        when(jwtUtil.getCategory(nonExistentToken)).thenReturn("refresh");
        when(refreshRepository.existsByRefresh(nonExistentToken)).thenReturn(false);

        // when & then
        assertThatThrownBy( () ->
                reissueService.validateRefreshToken(nonExistentToken))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_EXISTED_REFRESH_TOKEN.getMessage());
    }

    @Test
    @DisplayName("존재하는 리프레시 토큰일 때 예외 발생하지 않음")
    void validateRefreshToken5() {
        // given
        String existingToken = "existingToken";
        when(jwtUtil.getCategory(existingToken)).thenReturn("refresh");
        when(refreshRepository.existsByRefresh(existingToken)).thenReturn(true);

        // when & then
        assertDoesNotThrow(() -> reissueService.validateRefreshToken(existingToken));
    }

    @Test
    @DisplayName("새로운 리프레시 토큰 생성")
    void getNewRefreshToken() {
        // given
        String refreshToken = "refreshToken";
        String userUUID = "userUUID";
        when(jwtUtil.getUuid(refreshToken)).thenReturn(userUUID);

        TokenResponse mockTokenResponse = new TokenResponse("accessToken", 900000L, "refreshToken", 2592000000L);
        when(jwtService.generateJwtToken(any(UserResponse.class))).thenReturn(mockTokenResponse);

        // when
        TokenResponse response = reissueService.getNewRefreshToken(refreshToken);

        // then
        assertEquals("accessToken", response.accessToken());
        assertEquals("refreshToken", response.refreshToken());
        verify(refreshRepository, times(1)).deleteByRefresh(refreshToken);
    }

    @Test
    @DisplayName("리프레시 토큰 삭제")
    void deleteRefreshToken() {
        // given
        String refreshToken = "refreshToken";

        // when
        reissueService.deleteRefreshToken(refreshToken);

        // then
        verify(refreshRepository, times(1)).deleteByRefresh(refreshToken);
    }

    @Test
    @DisplayName("리프레시 토큰 존재 여부 확인")
    void isRefreshTokenExisted() {
        // Given
        String refreshToken = "refreshToken";
        when(refreshRepository.existsByRefresh(refreshToken)).thenReturn(true);

        // When
        boolean exists = reissueService.isRefreshTokenExisted(refreshToken);

        // Then
        assertTrue(exists);
    }
}