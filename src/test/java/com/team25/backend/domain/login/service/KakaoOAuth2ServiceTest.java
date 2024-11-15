package com.team25.backend.domain.login.service;

import com.team25.backend.domain.login.dto.request.LoginRequest;
import com.team25.backend.domain.user.dto.request.UserRequest;
import com.team25.backend.domain.user.dto.response.UserResponse;
import com.team25.backend.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KakaoOAuth2ServiceTest {
    @Mock
    private UserService userService;
    @InjectMocks
    private KakaoOAuth2Service kakaoOAuth2Service;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
        kakaoOAuth2Service = spy(kakaoOAuth2Service);
    }

    @Test
    @DisplayName("카카오 OAuth 서비스가 새 사용자를 등록하는 경우")
    void processKakaoLogin() {
        // given
        String oauthAccessToken = "mockAccessToken";
        String kakaoUsername = "kakao1234";
        LoginRequest loginRequest = new LoginRequest(oauthAccessToken);
        doReturn(kakaoUsername).when(kakaoOAuth2Service).getUserInfo(oauthAccessToken);

        // 사용자 미등록 상태
        UserRequest userRequest = new UserRequest(kakaoUsername);
        when(userService.isAlreadyUserRegister(userRequest)).thenReturn(false);

        // 사용자 등록
        UserResponse mockUserResponse = new UserResponse(kakaoUsername,"test-uuid","ROLE_USER");
        when(userService.registerUser(userRequest)).thenReturn(mockUserResponse);

        // when
        UserResponse userResponse = kakaoOAuth2Service.processKakaoLogin(loginRequest);

        // then
        assertEquals(mockUserResponse, userResponse);
        verify(userService, times(1)).isAlreadyUserRegister(userRequest);
        verify(userService, times(1)).registerUser(userRequest);
    }

    @Test
    @DisplayName("카카오 OAuth 서비스가 기존 사용자를 찾는 경우")
    void processKakaoLogin2() {
        // given
        String oauthAccessToken = "mockAccessToken";
        String kakaoUsername = "kakao1234";
        LoginRequest loginRequest = new LoginRequest(oauthAccessToken);
        doReturn(kakaoUsername).when(kakaoOAuth2Service).getUserInfo(oauthAccessToken);

        // 사용자 등록 상태
        UserRequest userRequest = new UserRequest(kakaoUsername);
        when(userService.isAlreadyUserRegister(userRequest)).thenReturn(true);

        // 사용자 찾기
        UserResponse mockUserResponse = new UserResponse(kakaoUsername,"test-uuid","ROLE_USER");
        when(userService.findUser(userRequest)).thenReturn(mockUserResponse);

        // when
        UserResponse userResponse = kakaoOAuth2Service.processKakaoLogin(loginRequest);

        // then
        assertEquals(mockUserResponse, userResponse);
        verify(userService, times(1)).isAlreadyUserRegister(userRequest);
        verify(userService, times(1)).findUser(userRequest);
    }
}