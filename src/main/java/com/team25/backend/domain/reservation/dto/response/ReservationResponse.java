package com.team25.backend.domain.reservation.dto.response;

import com.team25.backend.domain.patient.dto.response.PatientResponse;
import com.team25.backend.domain.reservation.enumdomain.ReservationStatus;
import com.team25.backend.domain.reservation.enumdomain.ServiceType;
import com.team25.backend.domain.reservation.enumdomain.Transportation;
import java.time.LocalDateTime;

public record ReservationResponse(
    Long reservationId,
    Long managerId,
    String departureLocation,
    String arrivalLocation,
    LocalDateTime reservationDateTime,
    ServiceType serviceType,
    Transportation transportation,
    int price,
    ReservationStatus reservationStatus,
    PatientResponse patient
) {
}