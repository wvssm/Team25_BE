package com.team25.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExpireBillingKeyResponse(
        String resultCode,
        String resultMsg,
        String tid,
        String orderId,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String bid,
        String authDate
) {}
