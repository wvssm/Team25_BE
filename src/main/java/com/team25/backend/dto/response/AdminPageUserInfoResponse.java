package com.team25.backend.dto.response;

public record AdminPageUserInfoResponse(Long id,
                                        String username,
                                        String role,
                                        String description) {
}
