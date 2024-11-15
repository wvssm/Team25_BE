package com.team25.backend.global.security.handler;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CustomAuthenticationEntryPointTest {

    @InjectMocks
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 /admin 경로에 접근할 때 로그인 페이지로 리다이렉트 된다")
    void commence_인증되지_않은_사용자_로그인_페이지로_리다이렉트() throws IOException {
        // given
        request.setRequestURI("/admin");
        AuthenticationException authException = new AuthenticationException("Unauthorized") {};

        // when
        customAuthenticationEntryPoint.commence(request, response, authException);

        // then
        assertEquals("/login", response.getRedirectedUrl());
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 /admin 이외의 경로에 접근할 때, 401 상태코드와 에러 메시지가 반환된다")
    void commence_인증되지_않은_사용자_401에러코드와_에러메시지() throws IOException {
        // given
        request.setRequestURI("/user/profile");
        AuthenticationException authException = new AuthenticationException("Unauthorized") {};

        // when
        customAuthenticationEntryPoint.commence(request, response, authException);

        // then
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("{\"status\": false, \"message\": \"인증되지 않은 사용자입니다.\", \"data\": null}", response.getContentAsString());
    }
}