package com.team25.backend.domain.user.entity;

import com.team25.backend.domain.manager.entity.Manager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testUser", "test-uuid", "ROLE_USER");
    }

    @Test
    @DisplayName("User의 역할이 정상적으로 업데이트 된다")
    void updateRole() {
        // given
        String newRole = "ROLE_ADMIN";

        // when
        user.updateRole(newRole);

        // then
        assertEquals(newRole, user.getRole());
    }

    @Test
    @DisplayName("Manager를 User에 설정한다")
    void setManager() {
        // given
        Manager manager = new Manager();

        // when
        user.setManager(manager);

        // then
        assertEquals(manager, user.getManager());
    }

    @Test
    @DisplayName("Manager가 null로 설정되면 기존 Manager가 삭제된다")
    void removeManager() {
        // given
        Manager manager = new Manager();
        user.setManager(manager);

        // when
        user.setManager(null);

        // then
        assertNull(user.getManager());
    }
}