package com.team25.backend.dto.response;

import java.util.List;

public record AdminPageResponse(
        Long userId,
        String username,
        String role,
        Long managerId,
        String managerName,
        List<String> certificates
) {}
