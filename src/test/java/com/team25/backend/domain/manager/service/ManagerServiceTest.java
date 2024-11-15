package com.team25.backend.domain.manager.service;

import com.team25.backend.domain.manager.dto.request.*;
import com.team25.backend.domain.manager.dto.response.*;
import com.team25.backend.domain.manager.entity.Manager;
import com.team25.backend.domain.manager.entity.Certificate;
import com.team25.backend.domain.manager.entity.WorkingHour;
import com.team25.backend.domain.manager.repository.ManagerRepository;
import com.team25.backend.domain.manager.repository.CertificateRepository;
import com.team25.backend.domain.manager.repository.WorkingHourRepository;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.global.exception.CustomException;
import com.team25.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.team25.backend.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagerServiceTest {

    @InjectMocks
    private ManagerService managerService;

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private WorkingHourRepository workingHourRepository;

    @Test
    @DisplayName("매니저 등록 성공 테스트")
    void createManager_Success() {
        // Given
        User user = new User("testuser", "uuid", "ROLE_USER");
        User spyUser = spy(user);

        doReturn(1L).when(spyUser).getId();

        ManagerCreateRequest request = new ManagerCreateRequest(
            "Manager Name", "profile.jpg", "5 years", "Comment", "certificate.jpg", "남성"
        );

        when(managerRepository.existsByUserId(spyUser.getId())).thenReturn(false);

        Manager savedManager = Manager.builder()
            .id(1L)
            .user(spyUser)
            .build();

        when(managerRepository.save(any(Manager.class))).thenReturn(savedManager);

        // When
        ManagerCreateResponse response = managerService.createManager(spyUser, request);

        // Then
        assertThat(response).isNotNull();

        // Verify
        verify(managerRepository).existsByUserId(spyUser.getId());
        verify(managerRepository).save(any(Manager.class));
        verify(certificateRepository).save(any(Certificate.class));
    }

    @Test
    @DisplayName("매니저 등록 실패 테스트 - 이미 매니저인 경우")
    void createManager_Failure_AlreadyManager() {
        // Given
        User user = new User("testuser", "uuid", "ROLE_USER");
        User spyUser = spy(user);

        doReturn(1L).when(spyUser).getId();

        ManagerCreateRequest request = new ManagerCreateRequest(
            "Manager Name", "profile.jpg", "5 years", "Comment", "certificate.jpg", "남성"
        );

        when(managerRepository.existsByUserId(spyUser.getId())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> managerService.createManager(spyUser, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(MANAGER_ALREADY_EXISTS.getMessage());

        // Verify
        verify(managerRepository).existsByUserId(spyUser.getId());
        verify(managerRepository, never()).save(any(Manager.class));
    }

    @Test
    @DisplayName("매니저 생성 실패 테스트 - 이름이 비어있는 경우")
    void createManager_InvalidName() {
        // Given
        User user = new User("testuser", "uuid", "ROLE_USER");
        User spyUser = spy(user);

        doReturn(1L).when(spyUser).getId();

        ManagerCreateRequest request = new ManagerCreateRequest(
            "",
            "profile.jpg", "5 years", "Comment", "certificate.jpg", "남성"
        );

        when(managerRepository.existsByUserId(spyUser.getId())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> managerService.createManager(spyUser, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.INVALID_INPUT_VALUE.getMessage());

        // Verify
        verify(managerRepository).existsByUserId(spyUser.getId());
        verify(managerRepository, never()).save(any(Manager.class));
    }

    @Test
    @DisplayName("매니저 프로필 조회 실패 테스트 - 존재하지 않는 매니저")
    void getManagerProfile_Failure_NotFound() {
        // Given
        Long managerId = 1L;

        when(managerRepository.findById(managerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> managerService.getManagerProfile(managerId))
            .isInstanceOf(CustomException.class)
            .hasMessage(MANAGER_NOT_FOUND.getMessage());

        // Verify
        verify(managerRepository).findById(managerId);
    }

    @Test
    @DisplayName("근무 시간 업데이트 성공 테스트")
    void updateWorkingHour_Success() {
        // Given
        User user = new User("testuser", "uuid", "ROLE_USER");
        User spyUser = spy(user);

        doReturn(1L).when(spyUser).getId();

        Manager manager = new Manager();
        manager.setUser(spyUser);
        spyUser.setManager(manager);

        WorkingHour workingHour = new WorkingHour();
        manager.setWorkingHour(workingHour);

        ManagerWorkingHourUpdateRequest request = new ManagerWorkingHourUpdateRequest(
            "09:00", "18:00",
            "09:00", "18:00",
            "09:00", "18:00",
            "09:00", "18:00",
            "09:00", "18:00",
            "00:00", "00:00",
            "00:00", "00:00"
        );

        // When
        ManagerWorkingHourUpdateResponse response = managerService.updateWorkingHour(spyUser, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.monStartTime()).isEqualTo("09:00");
        assertThat(response.monEndTime()).isEqualTo("18:00");

        // Verify
        verify(workingHourRepository).save(workingHour);
    }

    @Test
    @DisplayName("근무 시간 업데이트 실패 테스트 - 매니저가 아닌 경우")
    void updateWorkingHour_Failure_NotManager() {
        // Given
        User user = new User("testuser", "uuid", "ROLE_USER");
        User spyUser = spy(user);

        doReturn(1L).when(spyUser).getId();

        ManagerWorkingHourUpdateRequest request = new ManagerWorkingHourUpdateRequest(
            "09:00", "18:00",
            "09:00", "18:00",
            "09:00", "18:00",
            "09:00", "18:00",
            "09:00", "18:00",
            "00:00", "00:00",
            "00:00", "00:00"
        );

        // When & Then
        assertThatThrownBy(() -> managerService.updateWorkingHour(spyUser, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(MANAGER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("근무 시간 업데이트 실패 테스트 - 잘못된 시간 형식")
    void updateWorkingHour_InvalidTimeFormat() {
        // Given
        User user = new User("testuser", "uuid", "ROLE_USER");
        User spyUser = spy(user);

        doReturn(1L).when(spyUser).getId();

        Manager manager = new Manager();
        manager.setUser(spyUser);
        spyUser.setManager(manager);

        WorkingHour workingHour = new WorkingHour();
        manager.setWorkingHour(workingHour);

        ManagerWorkingHourUpdateRequest request = new ManagerWorkingHourUpdateRequest(
            "9시", "18:00", // 잘못된 시간 형식
            "09:00", "18:00",
            "09:00", "18:00",
            "09:00", "18:00",
            "09:00", "18:00",
            "00:00", "00:00",
            "00:00", "00:00"
        );

        // When & Then
        assertThatThrownBy(() -> managerService.updateWorkingHour(spyUser, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.INVALID_WORKING_HOUR_FORMAT.getMessage());

        // Verify
        verify(workingHourRepository, never()).save(any(WorkingHour.class));
    }

    @Test
    @DisplayName("근무 시간 업데이트 실패 테스트 - 시작 시간이 종료 시간 이후인 경우")
    void updateWorkingHour_InvalidTimeRange() {
        // Given
        User user = new User("testuser", "uuid", "ROLE_USER");
        User spyUser = spy(user);

        doReturn(1L).when(spyUser).getId();

        Manager manager = new Manager();
        manager.setUser(spyUser);
        spyUser.setManager(manager);

        WorkingHour workingHour = new WorkingHour();
        manager.setWorkingHour(workingHour);

        ManagerWorkingHourUpdateRequest request = new ManagerWorkingHourUpdateRequest(
            "18:00", "09:00", // 시작 시간이 종료 시간 이후
            "09:00", "18:00",
            "09:00", "18:00",
            "09:00", "18:00",
            "09:00", "18:00",
            "00:00", "00:00",
            "00:00", "00:00"
        );

        // When & Then
        assertThatThrownBy(() -> managerService.updateWorkingHour(spyUser, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.INVALID_TIME_RANGE.getMessage());

        // Verify
        verify(workingHourRepository, never()).save(any(WorkingHour.class));
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 성공 테스트")
    void updateProfileImage_Success() {
        // Given
        User user = new User("testuser", "uuid", "ROLE_USER");
        User spyUser = spy(user);

        doReturn(1L).when(spyUser).getId();

        Manager manager = new Manager();
        manager.setUser(spyUser);
        manager.setProfileImage("old_profile.jpg");
        spyUser.setManager(manager);

        ManagerProfileImageUpdateRequest request = new ManagerProfileImageUpdateRequest("new_profile.jpg");

        // When
        ManagerProfileImageUpdateResponse response = managerService.updateProfileImage(spyUser, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.profileImage()).isEqualTo("new_profile.jpg");
        assertThat(manager.getProfileImage()).isEqualTo("new_profile.jpg");

        // Verify
        verify(managerRepository).save(manager);
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 실패 테스트 - 매니저가 아닌 경우")
    void updateProfileImage_Failure_NotManager() {
        // Given
        User user = new User("testuser", "uuid", "ROLE_USER");
        User spyUser = spy(user);

        doReturn(1L).when(spyUser).getId();

        ManagerProfileImageUpdateRequest request = new ManagerProfileImageUpdateRequest("new_profile.jpg");

        // When & Then
        assertThatThrownBy(() -> managerService.updateProfileImage(spyUser, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(MANAGER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 실패 테스트 - 프로필 이미지가 null인 경우")
    void updateProfileImage_NullProfileImage() {
        // Given
        User user = new User("testuser", "uuid", "ROLE_USER");
        User spyUser = spy(user);

        doReturn(1L).when(spyUser).getId();

        Manager manager = new Manager();
        manager.setUser(spyUser);
        spyUser.setManager(manager);

        ManagerProfileImageUpdateRequest request = new ManagerProfileImageUpdateRequest(null);

        // When & Then
        assertThatThrownBy(() -> managerService.updateProfileImage(spyUser, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.INVALID_PROFILE_IMAGE.getMessage());

        // Verify
        verify(managerRepository, never()).save(any(Manager.class));
    }

    @Test
    @DisplayName("코멘트 업데이트 성공 테스트")
    void updateComment_Success() {
        // Given
        User user = new User("testuser", "uuid", "ROLE_USER");
        User spyUser = spy(user);

        doReturn(1L).when(spyUser).getId();

        Manager manager = new Manager();
        manager.setUser(spyUser);
        manager.setComment("Old comment");
        spyUser.setManager(manager);

        ManagerCommentUpdateRequest request = new ManagerCommentUpdateRequest("Updated comment");

        // When
        ManagerCommentUpdateResponse response = managerService.updateComment(spyUser, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.comment()).isEqualTo("Updated comment");
        assertThat(manager.getComment()).isEqualTo("Updated comment");

        // Verify
        verify(managerRepository).save(manager);
    }

    @Test
    @DisplayName("코멘트 업데이트 실패 테스트 - 매니저가 아닌 경우")
    void updateComment_Failure_NotManager() {
        // Given
        User user = new User("testuser", "uuid", "ROLE_USER");
        User spyUser = spy(user);

        doReturn(1L).when(spyUser).getId();

        ManagerCommentUpdateRequest request = new ManagerCommentUpdateRequest("Updated comment");

        // When & Then
        assertThatThrownBy(() -> managerService.updateComment(spyUser, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(MANAGER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("코멘트 업데이트 실패 테스트 - 코멘트가 빈 문자열인 경우")
    void updateComment_EmptyComment() {
        // Given
        User user = new User("testuser", "uuid", "ROLE_USER");
        User spyUser = spy(user);

        doReturn(1L).when(spyUser).getId();

        Manager manager = new Manager();
        manager.setUser(spyUser);
        spyUser.setManager(manager);

        ManagerCommentUpdateRequest request = new ManagerCommentUpdateRequest("");

        // When & Then
        assertThatThrownBy(() -> managerService.updateComment(spyUser, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.INVALID_COMMENT.getMessage());

        // Verify
        verify(managerRepository, never()).save(any(Manager.class));
    }

    @Test
    @DisplayName("근무 지역 업데이트 성공 테스트")
    void updateLocation_Success() {
        // Given
        User user = new User("testuser", "uuid", "ROLE_USER");
        User spyUser = spy(user);

        doReturn(1L).when(spyUser).getId();

        Manager manager = new Manager();
        manager.setUser(spyUser);
        manager.setWorkingRegion("Old Region");
        spyUser.setManager(manager);

        ManagerLocationUpdateRequest request = new ManagerLocationUpdateRequest("New Region");

        // When
        ManagerLocationUpdateResponse response = managerService.updateLocation(spyUser, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.workingRegion()).isEqualTo("New Region");
        assertThat(manager.getWorkingRegion()).isEqualTo("New Region");

        // Verify
        verify(managerRepository).save(manager);
    }

    @Test
    @DisplayName("근무 지역 업데이트 실패 테스트 - 매니저가 아닌 경우")
    void updateLocation_Failure_NotManager() {
        // Given
        User user = new User("testuser", "uuid", "ROLE_USER");
        User spyUser = spy(user);

        doReturn(1L).when(spyUser).getId();

        ManagerLocationUpdateRequest request = new ManagerLocationUpdateRequest("New Region");

        // When & Then
        assertThatThrownBy(() -> managerService.updateLocation(spyUser, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(MANAGER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("근무 지역 업데이트 실패 테스트 - 근무 지역이 null인 경우")
    void updateLocation_NullWorkingRegion() {
        // Given
        User user = new User("testuser", "uuid", "ROLE_USER");
        User spyUser = spy(user);

        doReturn(1L).when(spyUser).getId();

        Manager manager = new Manager();
        manager.setUser(spyUser);
        spyUser.setManager(manager);

        ManagerLocationUpdateRequest request = new ManagerLocationUpdateRequest(null);

        // When & Then
        assertThatThrownBy(() -> managerService.updateLocation(spyUser, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.INVALID_WORKING_REGION.getMessage());

        // Verify
        verify(managerRepository, never()).save(any(Manager.class));
    }

    @Test
    @DisplayName("매니저 이름 조회 성공 테스트")
    void findManagerNameByUserId_Success() {
        // Given
        Long userId = 1L;

        Manager manager = new Manager();
        manager.setManagerName("Manager Name");

        when(managerRepository.findByUserId(userId)).thenReturn(Optional.of(manager));

        // When
        ManagerNameResponse response = managerService.findManagerNameByUserId(userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.managerName()).isEqualTo("Manager Name");

        // Verify
        verify(managerRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("매니저 이름 조회 실패 테스트 - 매니저가 아닌 경우")
    void findManagerNameByUserId_Failure_NotManager() {
        // Given
        Long userId = 1L;

        when(managerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> managerService.findManagerNameByUserId(userId))
            .isInstanceOf(CustomException.class)
            .hasMessage(MANAGER_NOT_FOUND.getMessage());

        // Verify
        verify(managerRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("매니저 목록 조회 성공 테스트")
    void getManagersByDateAndRegion_Success() {
        // Given
        String date = "2024-11-11"; // 월요일
        String region = "서울";

        Manager manager = new Manager();
        manager.setId(1L);
        manager.setManagerName("Manager Name");
        manager.setProfileImage("profile.jpg");
        manager.setCareer("5 years");
        manager.setComment("Comment");
        manager.setGender("남성");
        manager.setWorkingRegion("서울");

        WorkingHour workingHour = new WorkingHour();
        workingHour.setMonStartTime("09:00");
        workingHour.setMonEndTime("18:00");
        manager.setWorkingHour(workingHour);

        List<Manager> managers = new ArrayList<>();
        managers.add(manager);

        when(managerRepository.findAll()).thenReturn(managers);

        // When
        List<ManagerByDateAndRegionResponse> responses = managerService.getManagersByDateAndRegion(date, region);

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses.size()).isEqualTo(1);
        ManagerByDateAndRegionResponse response = responses.getFirst();
        assertThat(response.managerId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Manager Name");

        // Verify
        verify(managerRepository).findAll();
    }

    @Test
    @DisplayName("매니저 목록 조회 실패 테스트 - 잘못된 날짜 형식")
    void getManagersByDateAndRegion_Failure_InvalidDateFormat() {
        // Given
        String date = "invalid-date";
        String region = "서울";

        // When & Then
        assertThatThrownBy(() -> managerService.getManagersByDateAndRegion(date, region))
            .isInstanceOf(CustomException.class)
            .hasMessage(INVALID_DATE_FORMAT.getMessage());

        // Verify
        verify(managerRepository, never()).findAll();
    }

    @Test
    @DisplayName("매니저 목록 조회 실패 테스트 - 존재하지 않는 지역")
    void getManagersByDateAndRegion_Failure_InvalidRegion() {
        // Given
        String date = "2023-10-05";
        String region = "없는지역";

        // When & Then
        assertThatThrownBy(() -> managerService.getManagersByDateAndRegion(date, region))
            .isInstanceOf(CustomException.class)
            .hasMessage(REGION_NOT_FOUND.getMessage());

        // Verify
        verify(managerRepository, never()).findAll();
    }
}