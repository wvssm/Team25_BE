package com.team25.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BillingKeyResponse(
        String resultCode,
        String resultMsg,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String bid,
        String authDate,
        String cardCode,
        String cardName,
        String tid,
        String orderId
) {}
