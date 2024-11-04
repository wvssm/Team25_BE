package com.team25.backend.dto.response;

import com.team25.backend.enumdomain.ReservationStatus;
import com.team25.backend.enumdomain.ServiceType;
import com.team25.backend.enumdomain.Transportation;
import java.time.LocalDateTime;
import org.springframework.validation.annotation.Validated;

@Validated
public record ReservationResponse(
    Long reservationId,
    Long managerId,
    String departureLocation,
    String arrivalLocation,
    LocalDateTime reservationDateTime,
    ServiceType serviceType,
    Transportation transportation,
    int price,
    ReservationStatus reservationStatus) {
}