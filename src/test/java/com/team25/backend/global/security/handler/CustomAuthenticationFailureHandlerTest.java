package com.team25.backend.global.security.handler;

import jakarta.servlet.ServletException;
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

class CustomAuthenticationFailureHandlerTest {
    @InjectMocks
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("인증 실패 시 로그인 페이지로 리다이렉트한다")
    void onAuthenticationFailure()  throws IOException, ServletException {
        // given
        AuthenticationException exception = new AuthenticationException("Authentication failed") {};

        // when
        customAuthenticationFailureHandler.onAuthenticationFailure(request, response, exception);

        // then
        assertEquals("/login?error=true", response.getRedirectedUrl());
    }
}