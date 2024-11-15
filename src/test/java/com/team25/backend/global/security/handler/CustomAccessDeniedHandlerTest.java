package com.team25.backend.global.security.handler;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CustomAccessDeniedHandlerTest {
    @InjectMocks
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("접근 권한이 없을 때(401) 에러 메시지를 반환한다")
    void handle() throws IOException {
        // given
        AccessDeniedException accessDeniedException = new AccessDeniedException("Forbidden");

        // when
        customAccessDeniedHandler.handle(request, response, accessDeniedException);

        // then
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertEquals("{\"status\": false, \"message\": \"API 사용 권한이 없습니다.\", \"data\": null}", response.getContentAsString());
    }
}