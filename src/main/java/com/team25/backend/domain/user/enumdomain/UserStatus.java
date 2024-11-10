package com.team25.backend.domain.user.enumdomain;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UserStatus {

    @JsonProperty("일반유저")
    NORMAL_USER,
    @JsonProperty("매니저승인대기")
    WATING_APPROVAL,
    @JsonProperty("매니저")
    MANAGER;
}
