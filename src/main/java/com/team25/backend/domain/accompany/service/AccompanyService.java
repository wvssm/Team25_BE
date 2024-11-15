package com.team25.backend.domain.accompany.service;

import static com.team25.backend.global.exception.ErrorCode.INVALID_ACCOMPANY_STATUS;
import static com.team25.backend.global.exception.ErrorCode.REQUIRED_DATE_MISSING;
import static com.team25.backend.global.exception.ErrorCode.REQUIRED_DESCRIPTION_MISSING;
import static com.team25.backend.global.exception.ErrorCode.RESERVATION_NOT_FOUND;
import static com.team25.backend.global.exception.ErrorCode.RESERVATION_WITHOUT_ACCOMPANY;

import com.team25.backend.domain.accompany.dto.request.AccompanyRequest;
import com.team25.backend.domain.accompany.dto.response.AccompanyResponse;
import com.team25.backend.domain.accompany.entity.Accompany;
import com.team25.backend.domain.accompany.enumdomain.AccompanyStatus;
import com.team25.backend.domain.accompany.repository.AccompanyRepository;
import com.team25.backend.domain.reservation.entity.Reservation;
import com.team25.backend.domain.reservation.repository.ReservationRepository;
import com.team25.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class AccompanyService {

    private final ReservationRepository reservationRepository;
    private final AccompanyRepository accompanyRepository;

    public AccompanyService(AccompanyRepository accompanyRepository,
        ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
        this.accompanyRepository = accompanyRepository;
    }

    public List<AccompanyResponse> getTrackingAccompanies(Long reservationId) {
        checkReservationNull(reservationId);
        List<Accompany> accompanies = accompanyRepository.findByReservation_id(reservationId);
        checkListEmpty(accompanies);
        return accompanies.stream().map(AccompanyService::getAccompanyResponse)
            .peek(response -> log.info("Accompany details: {}", response)).toList();
    }


    public AccompanyResponse addTrackingAccompany(Long reservationId,
        AccompanyRequest accompanyRequest) {
        validateAccompanyRequest(accompanyRequest);
        LocalDateTime accompanyDateTime = getLocalDateTime(accompanyRequest);
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
            () -> new CustomException(RESERVATION_NOT_FOUND));
        Accompany track = getAccompany(accompanyRequest, accompanyDateTime);
        accompanyRepository.save(track);
        reservation.addAccompany(track);
        reservationRepository.save(reservation);
        return getAccompanyResponse(track);
    }

    private static LocalDateTime getLocalDateTime(AccompanyRequest accompanyRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.parse(accompanyRequest.statusDate(), formatter);
    }

    private static AccompanyResponse getAccompanyResponse(Accompany track) {
        return new AccompanyResponse(track.getAccompanyStatus(), track.getTime(),
            track.getDetail());
    }


    private static Accompany getAccompany(AccompanyRequest accompanyRequest,
        LocalDateTime accompanyDateTime) {
        return Accompany.builder().accompanyStatus(accompanyRequest.status())
            .time(accompanyDateTime)
            .detail(accompanyRequest.statusDescribe())
            .build();
    }

    private void checkReservationNull(Long reservationId) {
        if (reservationRepository.findById(reservationId).isEmpty()) {
            throw new CustomException(RESERVATION_NOT_FOUND);
        }
    }

    private static void checkListEmpty(List<Accompany> searchedAccompanies) {
        if (searchedAccompanies.isEmpty()) {
            throw new CustomException(RESERVATION_WITHOUT_ACCOMPANY);
        }
    }

    private static void validateAccompanyRequest(AccompanyRequest accompanyRequest) {
        if (!Arrays.stream(AccompanyStatus.values()).toList().contains(accompanyRequest.status())) {
            throw new CustomException(INVALID_ACCOMPANY_STATUS);
        }
        if (accompanyRequest.statusDate().isEmpty()) {
            throw new CustomException(REQUIRED_DATE_MISSING);
        }
        if (accompanyRequest.statusDescribe().isEmpty()) {
            throw new CustomException(REQUIRED_DESCRIPTION_MISSING);
        }
    }

}
