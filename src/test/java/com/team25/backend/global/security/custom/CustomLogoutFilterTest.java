package com.team25.backend.global.security.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team25.backend.domain.login.dto.request.LogoutRequest;
import com.team25.backend.domain.login.service.ReissueService;
import com.team25.backend.global.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomLogoutFilterTest {

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private ReissueService reissueService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private CustomLogoutFilter customLogoutFilter;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customLogoutFilter = new CustomLogoutFilter(jwtUtil, reissueService, objectMapper);
    }

    @Test
    @DisplayName("유효한 로그아웃 요청 성공")
    void doFilter_validLogoutRequest() throws IOException, ServletException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String refreshToken = "validRefreshToken";
        request.setContent(objectMapper.writeValueAsBytes(new LogoutRequest(refreshToken)));

        when(jwtUtil.isExpired(refreshToken)).thenReturn(false);
        when(jwtUtil.getCategory(refreshToken)).thenReturn("refresh");
        when(reissueService.isRefreshTokenExisted(refreshToken)).thenReturn(true);

        // when
        customLogoutFilter.doFilter(request, response, filterChain);

        // then
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertEquals("{\"status\":true,\"message\":\"로그아웃을 성공했습니다.\",\"data\":null}", response.getContentAsString());
        verify(reissueService, times(1)).deleteRefreshToken(refreshToken);
    }

    @Test
    @DisplayName("만료된 Refresh 토큰 요청")
    void doFilter_expiredRefreshToken() throws IOException, ServletException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String refreshToken = "expiredRefreshToken";
        request.setContent(objectMapper.writeValueAsBytes(new LogoutRequest(refreshToken)));

        when(jwtUtil.isExpired(refreshToken)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // when
        customLogoutFilter.doFilter(request, response, filterChain);

        // then
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertEquals("{\"status\":false,\"message\":\"Refresh 토큰이 만료되었습니다.\",\"data\":null}", response.getContentAsString());
    }

    @Test
    @DisplayName("잘못된 형식의 Refresh 토큰 요청")
    void doFilter_malformedRefreshToken() throws IOException, ServletException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String refreshToken = "malformedToken";
        request.setContent(objectMapper.writeValueAsBytes(new LogoutRequest(refreshToken)));

        when(jwtUtil.isExpired(refreshToken)).thenThrow(new MalformedJwtException("Malformed token"));

        // when
        customLogoutFilter.doFilter(request, response, filterChain);

        // then
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertEquals("{\"status\":false,\"message\":\"잘못된 형식의 Refresh 토큰입니다.\",\"data\":null}", response.getContentAsString());
    }

    @Test
    @DisplayName("잘못된 서명의 Refresh 토큰 요청")
    void doFilter_signatureException() throws IOException, ServletException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String refreshToken = "invalidSignatureToken";
        request.setContent(objectMapper.writeValueAsBytes(new LogoutRequest(refreshToken)));

        when(jwtUtil.isExpired(refreshToken)).thenThrow(new SignatureException("Invalid signature"));

        // when
        customLogoutFilter.doFilter(request, response, filterChain);

        // then
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertEquals("{\"status\":false,\"message\":\"잘못된 서명의 Refresh 토큰입니다.\",\"data\":null}", response.getContentAsString());
    }

    @Test
    @DisplayName("존재하지 않는 Refresh 토큰 요청")
    void doFilter_nonExistentRefreshToken() throws IOException, ServletException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String refreshToken = "nonExistentToken";
        request.setContent(objectMapper.writeValueAsBytes(new LogoutRequest(refreshToken)));

        when(jwtUtil.isExpired(refreshToken)).thenReturn(false);
        when(jwtUtil.getCategory(refreshToken)).thenReturn("refresh");
        when(reissueService.isRefreshTokenExisted(refreshToken)).thenReturn(false);

        // when
        customLogoutFilter.doFilter(request, response, filterChain);

        // then
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertEquals("{\"status\":false,\"message\":\"해당 Refresh 토큰이 존재하지 않습니다.\",\"data\":null}", response.getContentAsString());
    }
}