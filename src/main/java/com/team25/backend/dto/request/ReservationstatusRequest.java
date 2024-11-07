package com.team25.backend.dto.request;

import com.team25.backend.enumdomain.ReservationStatus;

public record ReservationstatusRequest(
    ReservationStatus reservationStatus
) {

}
