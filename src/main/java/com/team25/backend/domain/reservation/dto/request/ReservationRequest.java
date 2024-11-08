package com.team25.backend.domain.reservation.dto.request;

import com.team25.backend.domain.patient.dto.request.PatientRequest;
import com.team25.backend.domain.reservation.enumdomain.ServiceType;
import com.team25.backend.domain.reservation.enumdomain.Transportation;

public record ReservationRequest(
    Long managerId,
    String departureLocation,
    String arrivalLocation,
    String reservationDateTime,
    ServiceType serviceType,
    Transportation transportation,
    int price,
    PatientRequest patient
) {

}