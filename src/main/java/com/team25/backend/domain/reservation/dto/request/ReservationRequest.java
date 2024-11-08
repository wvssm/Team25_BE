package com.team25.backend.domain.reservation.dto.request;

import com.team25.backend.global.annotation.ValidArrivalLocation;
import com.team25.backend.global.annotation.ValidDepartureLocation;
import com.team25.backend.global.annotation.ValidPrice;
import com.team25.backend.global.annotation.ValidServiceType;
import com.team25.backend.global.annotation.ValidTransportation;
import com.team25.backend.domain.patient.dto.request.PatientRequest;
import com.team25.backend.domain.reservation.enumdomain.ServiceType;
import com.team25.backend.domain.reservation.enumdomain.Transportation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

@Validated
public record ReservationRequest(
    @NotNull @Positive Long managerId,
    @ValidDepartureLocation String departureLocation,
    @ValidArrivalLocation String arrivalLocation,
    @NotNull(message = "예약 일시를 입력해 주십시오.")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}", message = "잘못된 날짜 형식입니다. 'yyyy-MM-dd HH:mm' 형식으로 입력해주세요.")
    String reservationDateTime,
    @ValidServiceType ServiceType serviceType,
    @ValidTransportation Transportation transportation,
    @Min(value = 0, message = "가격은 0 이상 입니다.")
    @ValidPrice int price,
    @NotNull PatientRequest patient
) {

}