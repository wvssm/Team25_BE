package com.team25.backend.domain.user.dto.response;

import com.team25.backend.domain.user.enumdomain.UserStatus;

public record UserRoleStatusResponse(
    UserStatus userStatus
) {
}
