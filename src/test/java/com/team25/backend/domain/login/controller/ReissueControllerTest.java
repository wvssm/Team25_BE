package com.team25.backend.domain.login.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team25.backend.domain.login.dto.request.ReissueRequest;
import com.team25.backend.domain.login.dto.response.TokenResponse;
import com.team25.backend.domain.login.service.ReissueService;
import com.team25.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReissueController.class)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
class ReissueControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReissueService reissueService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    @DisplayName("POST /auth/refresh - 토큰 재발급")
    void reissueToken() throws Exception {
        // given
        String refreshToken = "sample_refresh_token";
        ReissueRequest reissueRequest = new ReissueRequest(refreshToken);
        TokenResponse tokenResponse = new TokenResponse("new_access_token", 3600L, "new_refresh_token", 7200L);

        // ReissueService 동작 모의
        doNothing().when(reissueService).validateRefreshToken(refreshToken);
        when(reissueService.getNewRefreshToken(refreshToken)).thenReturn(tokenResponse);

        // expected
        mvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reissueRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("토큰이 재발급 되었습니다."))
                .andExpect(jsonPath("$.data.accessToken").value("new_access_token"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600L))
                .andExpect(jsonPath("$.data.refreshToken").value("new_refresh_token"))
                .andExpect(jsonPath("$.data.refreshTokenExpiresIn").value(7200L));

        // validate method calls
        verify(reissueService, times(1)).validateRefreshToken(refreshToken);
        verify(reissueService, times(1)).getNewRefreshToken(refreshToken);
    }
}