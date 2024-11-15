package com.team25.backend.domain.user.service;

import com.team25.backend.domain.admin.dto.response.AdminPageUserInfoResponse;
import com.team25.backend.domain.manager.entity.Manager;
import com.team25.backend.domain.user.dto.request.UserRequest;
import com.team25.backend.domain.user.dto.response.UserResponse;
import com.team25.backend.domain.user.dto.response.UserStatusResponse;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.domain.user.repository.UserRepository;
import com.team25.backend.global.exception.CustomException;
import jakarta.validation.constraints.AssertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.team25.backend.global.exception.ErrorCode.USER_ALREADY_EXISTS;
import static com.team25.backend.global.exception.ErrorCode.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("새로운 사용자가 등록되는 경우")
    void registerUser() {
        // given
        UserRequest userRequest = new UserRequest("user");
        when(userRepository.existsByUsername(userRequest.username())).thenReturn(false);

        User newUser = new User("user", "test-uuid", "ROLE_USER");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // when
        UserResponse userResponse = userService.registerUser(userRequest);

        // then
        assertNotNull(userResponse);
        assertEquals("user", userResponse.username());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("이미 등록된 사용자를 등록하려고 할 때 예외 발생")
    void registerUser2() {
        // given
        UserRequest userRequest = new UserRequest("user");
        when(userRepository.existsByUsername(userRequest.username())).thenReturn(true);

        // when & then
        assertThatThrownBy(() ->userService.registerUser(userRequest))
                .isInstanceOf(CustomException.class)
                .hasMessage(USER_ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("등록된 사용자가 있는지 확인")
    void isAlreadyUserRegister() {
        // given
        UserRequest userRequest = new UserRequest("user");
        when(userRepository.existsByUsername(userRequest.username())).thenReturn(true);

        // when
        boolean result = userService.isAlreadyUserRegister(userRequest);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("사용자를 찾은 경우")
    void findUser() {
        // given
        UserRequest userRequest = new UserRequest("user");
        User foundUser = new User("user", "test-uuid", "ROLE_USER");
        when(userRepository.findByUsername(userRequest.username())).thenReturn(Optional.of(foundUser));

        // when
        UserResponse userResponse = userService.findUser(userRequest);

        // then
        assertEquals("user", userResponse.username());
        assertEquals("test-uuid",userResponse.uuid());
        assertEquals("ROLE_USER",userResponse.role());
        verify(userRepository, times(1)).findByUsername(any(String.class));
    }

    @Test
    @DisplayName("사용자를 찾을 수 없는 경우 예외 발생")
    void findUser2() {
        // given
        UserRequest userRequest = new UserRequest("user");
        when(userRepository.findByUsername(userRequest.username())).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> userService.findUser(userRequest));
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자 삭제 시 사용자를 찾을 수 없는 경우 예외 발생")
    void removeUser_userNotFound() {
        // given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> userService.removeUser(userId));
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("Admin 페이지용 모든 사용자 정보를 반환")
    void getAllUsersForAdminPage() {
        // given
        User user1 = new User("user1", "uuid1", "ROLE_USER");
        User user2 = new User("user2", "uuid2", "ROLE_USER");
        user2.setManager(new Manager());
        User user3 = new User("user3", "uuid3", "ROLE_MANAGER");
        user3.setManager(new Manager());

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));

        // when
        List<AdminPageUserInfoResponse> responses = userService.getAllUsersForAdminPage();

        // then
        assertEquals(3, responses.size());
        assertEquals("일반 유저", responses.get(0).description());
        assertEquals("매니저 승인 대기", responses.get(1).description());
        assertEquals("매니저", responses.get(2).description());
    }

    @Test
    @DisplayName("특정 사용자 ID로 사용자 상태를 반환 - 일반 사용자")
    void getUserStatusById_user() {
        // given
        User user = new User("user1", "uuid1", "ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        UserStatusResponse response = userService.getUserStatusById(1L);

        // then
        assertEquals("USER", response.status());
    }

    @Test
    @DisplayName("특정 사용자 ID로 사용자 상태를 반환 - 매니저 승인 대기")
    void getUserStatusById_managerPending() {
        // given
        User user = new User("user2", "uuid2", "ROLE_USER");
        user.setManager(new Manager());
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        // when
        UserStatusResponse response = userService.getUserStatusById(2L);

        // then
        assertEquals("MANAGER_PENDING", response.status());
    }

    @Test
    @DisplayName("특정 사용자 ID로 사용자 상태를 반환 - 매니저")
    void getUserStatusById_manager() {
        // given
        User user = new User("user3", "uuid3", "ROLE_MANAGER");
        user.setManager(new Manager());
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));

        // when
        UserStatusResponse response = userService.getUserStatusById(3L);

        // then
        assertEquals("MANAGER", response.status());
    }

    @Test
    @DisplayName("특정 사용자 ID로 사용자를 찾을 수 없는 경우 예외 발생")
    void getUserStatusById_userNotFound() {
        // given
        when(userRepository.findById(4L)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> userService.getUserStatusById(4L));
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }
}