package com.team25.backend.domain.user.controller;

import com.team25.backend.domain.user.dto.response.UserRoleResponse;
import com.team25.backend.domain.user.dto.response.UserStatusResponse;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.domain.user.repository.UserRepository;
import com.team25.backend.domain.user.service.UserService;
import com.team25.backend.global.resolver.CustomAuthenticationPrincipalArgumentResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ExtendWith(SpringExtension.class)
@WithMockUser
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CustomAuthenticationPrincipalArgumentResolver customAuthenticationPrincipalArgumentResolver;

    private User user;

    @BeforeEach
    void setUp() throws Exception {
        this.user = new User("user", "tesetuuid", "ROLE_USER");
        when(customAuthenticationPrincipalArgumentResolver.supportsParameter(any())).thenReturn(
                true);
        when(customAuthenticationPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(user);
    }

    @Test
    @DisplayName("GET /api/users/me/role - 사용자 역할 조회")
    @WithMockUser
    void getMyRole() throws Exception {
        // given
        UserRoleResponse userRoleResponse = new UserRoleResponse(user.getRole());
        when(userService.getUserStatusById(user.getId())).thenReturn(new UserStatusResponse("USER")); // 모의 동작 설정

        // expected
        mvc.perform(get("/api/users/me/role")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("사용자 역할 조회를 성공했습니다."))
                .andExpect(jsonPath("$.data.userRole").value(user.getRole()))
                .andDo(print());
    }

    @Test
    @DisplayName("GET /api/users/me/status - 사용자 상태 조회")
    @WithMockUser
    void getMyStatus() throws Exception {
        // given
        UserStatusResponse userStatusResponse = new UserStatusResponse("USER");
        when(userService.getUserStatusById(user.getId())).thenReturn(userStatusResponse);

        // expected
        mvc.perform(get("/api/users/me/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("사용자 상태 조회를 성공했습니다."))
                .andExpect(jsonPath("$.data.status").value("USER"))
                .andDo(print());

        verify(userService, times(1)).getUserStatusById(user.getId());
    }

    @Test
    @DisplayName("DELETE /api/users/withdraw - 사용자 계정 삭제")
    @WithMockUser
    void deleteMyAccount() throws Exception {
        // expected
        mvc.perform(delete("/api/users/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("사용자를 삭제했습니다."))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andDo(print());

        verify(userService, times(1)).removeUser(user.getId());
    }
}