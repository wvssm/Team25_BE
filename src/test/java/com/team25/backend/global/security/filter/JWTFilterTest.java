package com.team25.backend.global.security.filter;

import com.team25.backend.domain.user.entity.User;
import com.team25.backend.domain.user.repository.UserRepository;
import com.team25.backend.global.security.dto.CustomUserDetails;
import com.team25.backend.global.security.filter.JWTFilter;
import com.team25.backend.global.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static com.team25.backend.global.exception.ErrorCode.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JWTFilterTest {

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JWTFilter customJwtFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("유효한 Access 토큰일 경우 필터를 통과한다")
    void doFilterInternal_validAccessToken() throws ServletException, IOException {
        // given (유효한 토큰일 경우)
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String validAccessToken = "Bearer validToken";
        request.addHeader("Authorization", validAccessToken);

        when(jwtUtil.isExpired("validToken")).thenReturn(false);
        when(jwtUtil.getCategory("validToken")).thenReturn("access");
        when(jwtUtil.getUuid("validToken")).thenReturn("userUUID");

        User user = new User("username", "userUUID", "ROLE_USER");
        when(userRepository.findByUuid("userUUID")).thenReturn(Optional.of(user));

        // when
        customJwtFilter.doFilterInternal(request, response, filterChain);

        // then (통과)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("username", ((CustomUserDetails) authentication.getPrincipal()).getUsername());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("만료된 Access 토큰의 요청일 경우 에러 메시지를 반환한다")
    void doFilterInternal_expiredToken() throws ServletException, IOException {
        // given (만료된 토큰일 경우)
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String expiredToken = "Bearer expiredToken";
        request.addHeader("Authorization", expiredToken);

        when(jwtUtil.isExpired("expiredToken")).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // when
        customJwtFilter.doFilterInternal(request, response, filterChain);

        // then
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("{\"status\": false, \"message\": \"Access token이 만료되었습니다.\", \"data\": null}", response.getContentAsString());
    }

    @Test
    @DisplayName("잘못된 형식의 토큰 요청일 경우 에러 메시지를 반환한다")
    void doFilterInternal_malformedToken() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String malformedToken = "Bearer malformedToken";
        request.addHeader("Authorization", malformedToken);

        when(jwtUtil.isExpired("malformedToken")).thenThrow(new MalformedJwtException("Malformed token"));

        // when
        customJwtFilter.doFilterInternal(request, response, filterChain);

        // then
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("{\"status\": false, \"message\": \"잘못된 토큰 형식입니다.\", \"data\": null}", response.getContentAsString());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 UUID로 인한 인증 실패할 경우 404 에러를 발생한다")
    void doFilterInternal_userNotFound() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String validAccessToken = "Bearer validToken";
        request.addHeader("Authorization", validAccessToken);

        when(jwtUtil.isExpired("validToken")).thenReturn(false);
        when(jwtUtil.getCategory("validToken")).thenReturn("access");
        when(jwtUtil.getUuid("validToken")).thenReturn("nonExistentUUID");
        when(userRepository.findByUuid("nonExistentUUID")).thenReturn(Optional.empty());

        // when
        customJwtFilter.doFilterInternal(request, response, filterChain);

        // then
        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
        assertEquals("{\"status\": false, \"message\": \"" + USER_NOT_FOUND.getMessage() + "\", \"data\": null}", response.getContentAsString());
    }
}