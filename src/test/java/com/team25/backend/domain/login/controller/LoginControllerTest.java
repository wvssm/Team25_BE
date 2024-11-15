package com.team25.backend.domain.login.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team25.backend.domain.login.dto.request.LoginRequest;
import com.team25.backend.domain.login.dto.response.TokenResponse;
import com.team25.backend.domain.login.service.JWTService;
import com.team25.backend.domain.login.service.KakaoOAuth2Service;
import com.team25.backend.domain.user.dto.response.UserResponse;
import com.team25.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(LoginController.class)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private KakaoOAuth2Service kakaoOAuth2Service;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /auth/kakao/login - 카카오 로그인")
    void loginWithKakao() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest("sample_auth_code");
        UserResponse userInfo = new UserResponse("jinyoung", "test_uuid", "ROLE_USER");
        TokenResponse tokenResponse = new TokenResponse("access_token", 3600L, "refresh_token", 7200L);

        // KakaoOAuth2Service와 JWTService 동작 모의
        when(kakaoOAuth2Service.processKakaoLogin(loginRequest)).thenReturn(userInfo);
        when(jwtService.generateJwtToken(userInfo)).thenReturn(tokenResponse);

        // expected
        mvc.perform(post("/auth/kakao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("로그인을 성공했습니다."))
                .andExpect(jsonPath("$.data.accessToken").value("access_token"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600L))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh_token"))
                .andExpect(jsonPath("$.data.refreshTokenExpiresIn").value(7200L));

        // 메서드 호출 검증
        verify(kakaoOAuth2Service, times(1)).processKakaoLogin(loginRequest);
        verify(jwtService, times(1)).generateJwtToken(userInfo);
    }
}