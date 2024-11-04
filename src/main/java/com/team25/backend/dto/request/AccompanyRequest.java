package com.team25.backend.dto.request;

import com.team25.backend.enumdomain.AccompanyStatus;
import java.io.Serializable;

public record AccompanyRequest(
    AccompanyStatus status,
    Double latitude,
    Double longitude,
    String statusDate,
    String statusDescribe) implements
    Serializable {

}