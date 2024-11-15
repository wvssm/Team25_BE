package com.team25.backend.domain.admin.service;

import com.team25.backend.domain.user.entity.User;
import com.team25.backend.domain.admin.dto.response.AdminPageResponse;
import com.team25.backend.domain.manager.entity.Manager;
import com.team25.backend.domain.user.repository.UserRepository;
import com.team25.backend.domain.manager.entity.Certificate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private AdminService adminService;

    private User user;
    private Manager manager;
    private Certificate certificate;
    private URL url;

    @BeforeEach
    void setUp() throws MalformedURLException {
        MockitoAnnotations.openMocks(this);

        this.manager = new Manager();
        this.manager.setManagerName("testManager");

        this.certificate = new Certificate(1L, "/abcd", manager);
        this.manager.setCertificates(List.of(certificate));

        this.user = new User("testUser", "testUUID", "ROLE_USER");
        this.user.setManager(manager);

        url = new URL("https://example.com/presigned-url");
    }

    @Test
    @DisplayName("매니저와 연관 관계가 있는 모든 사용자를 올바르게 조회하는지 확인")
    void getAllUsersWithManagers() {
        // given
        when(userRepository.findUsersWithManager()).thenReturn(List.of(user));
        when(s3Service.generatePresignedUrl(anyString(), anyString(), any(Duration.class)))
                .thenReturn(url);

        // when
        List<AdminPageResponse> responses = adminService.getAllUsersWithManagers();

        // then
        assertEquals(1, responses.size());
        AdminPageResponse response = responses.get(0);
        assertEquals(user.getId(), response.userId());
        assertEquals(user.getUsername(), response.username());
        assertEquals(user.getRole(), response.role());
        assertEquals(manager.getId(), response.managerId());
        assertEquals(manager.getManagerName(), response.managerName());
        assertEquals(1, response.certificates().size());
        assertEquals("https://example.com/presigned-url", response.certificates().get(0));

        verify(userRepository, times(1)).findUsersWithManager();
        verify(s3Service, times(1)).generatePresignedUrl(anyString(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("사용자 역할 변경이 올바르게 되는지 검사")
    void changeUserRole() {
        // given
        Long userId = user.getId();
        String newRole = "ROLE_ADMIN";
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        adminService.changeUserRole(userId, newRole);

        // then
        assertEquals(newRole, user.getRole());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }
}
