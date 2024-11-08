package com.team25.backend.domain.reservation.service;

import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_ARRIVAL_ADDRESS;
import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_DEPARTRUE_ADDRESS;
import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_SERVICE_TYPE;
import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_TRANSPORTATION_TYPE;
import static com.team25.backend.global.exception.ReservationErrorCode.MANAGER_NOT_FOUND;
import static com.team25.backend.global.exception.ReservationErrorCode.MANAGER_REQUIRED;
import static com.team25.backend.global.exception.ReservationErrorCode.RESERVATION_ALREADY_CANCELED;
import static com.team25.backend.global.exception.ReservationErrorCode.RESERVATION_NOT_BELONG_TO_USER;
import static com.team25.backend.global.exception.ReservationErrorCode.RESERVATION_NOT_FOUND;
import static com.team25.backend.global.exception.ReservationErrorCode.USER_NOT_FOUND;

import com.team25.backend.domain.manager.entity.Manager;
import com.team25.backend.domain.manager.repository.ManagerRepository;
import com.team25.backend.domain.patient.dto.response.PatientResponse;
import com.team25.backend.domain.patient.entity.Patient;
import com.team25.backend.domain.patient.service.PatientService;
import com.team25.backend.domain.reservation.dto.request.CancelRequest;
import com.team25.backend.domain.reservation.dto.request.ReservationRequest;
import com.team25.backend.domain.reservation.dto.request.ReservationstatusRequest;
import com.team25.backend.domain.reservation.dto.response.ReservationResponse;
import com.team25.backend.domain.reservation.entity.Reservation;
import com.team25.backend.domain.reservation.enumdomain.CancelReason;
import com.team25.backend.domain.reservation.enumdomain.ReservationStatus;
import com.team25.backend.domain.reservation.enumdomain.ServiceType;
import com.team25.backend.domain.reservation.enumdomain.Transportation;
import com.team25.backend.domain.reservation.repository.ReservationRepository;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.global.exception.ReservationErrorCode;
import com.team25.backend.global.exception.ReservationException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ManagerRepository managerRepository;
    private final PatientService patientService;

    public ReservationService(ReservationRepository reservationRepository,
        ManagerRepository managerRepository
        , PatientService patientService) {
        this.reservationRepository = reservationRepository;
        this.managerRepository = managerRepository;
        this.patientService = patientService;
    }

    // 예약 전체 조회
    public List<ReservationResponse> getAllReservations(User user) {
        List<Reservation> reservations = reservationRepository.findByUser_Uuid(user.getUuid());
        if (reservations.isEmpty()) {
            throw new ReservationException(RESERVATION_NOT_FOUND);
        }
        List<ReservationResponse> responseList = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (!reservation.getReservationStatus().equals(ReservationStatus.CANCEL)) {
                responseList.add(getReservationResponse(reservation));
            }
        }
        return responseList;
    }

    // 단일 예약 조회
    public ReservationResponse getReservationById(User user, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ReservationException(RESERVATION_NOT_FOUND));
        List<Reservation> reservations = reservationRepository.findByUser_Uuid(user.getUuid());
        if (reservations.isEmpty()) {
            throw new ReservationException(USER_NOT_FOUND);
        }
        if (!reservations.contains(reservation)) {
            throw new ReservationException(RESERVATION_NOT_BELONG_TO_USER);
        }

        return getReservationResponse(reservation);
    }

    // 예약 작성
    public ReservationResponse createReservation(ReservationRequest reservationRequest, User user) {
        validateReservationRequest(reservationRequest);
        LocalDateTime reservationDateTime = getLocalDateTime(reservationRequest);
        Manager manager = managerRepository.findById(reservationRequest.managerId())
            .orElseThrow(() -> new ReservationException(MANAGER_NOT_FOUND));
        Patient patient = patientService.addPatient(reservationRequest.patient());
        Reservation reservation = getReservation(reservationRequest, user, reservationDateTime, patient, manager);
        reservationRepository.save(reservation);
        return getReservationResponse(reservation);
    }

    // 예약 취소
    @Transactional
    public ReservationResponse cancelReservation(User user, CancelRequest cancelRequest,
        Long reservationId) {

        List<Reservation> reservations = reservationRepository.findByUser_Uuid(user.getUuid());
        if (reservations.isEmpty()) {
            throw new ReservationException(USER_NOT_FOUND);
        }
        Reservation canceledReservation = reservations.stream()
            .filter(x -> x.getId().equals(reservationId)).findFirst()
            .orElseThrow(() -> new ReservationException(RESERVATION_NOT_FOUND));
        if (canceledReservation.getReservationStatus() == ReservationStatus.CANCEL) {
            throw new ReservationException(RESERVATION_ALREADY_CANCELED);
        }
        CancelReason cancelReason = cancelRequest.cancelReason();
        addCancelReasonAndDetail(canceledReservation, cancelReason,
            cancelRequest.cancelDetail()); // 예약에 취소 사유와 상세 정보 추가
        reservationRepository.save(canceledReservation);
        return getReservationResponse(canceledReservation);
    }

    // 예약 상태 변경
    public ReservationResponse changeReservationStatus(User user, Long reservationId,
        ReservationstatusRequest reservationstatusRequest) {
        if (!Arrays.stream(ReservationStatus.values()).toList()
            .contains(reservationstatusRequest.reservationStatus())) {
            throw new ReservationException(ReservationErrorCode.INVALID_RESERVATION_STATUS);
        }
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ReservationException(RESERVATION_NOT_FOUND));
        if (!user.getReservations().contains(reservation)) {
            throw new ReservationException(RESERVATION_NOT_BELONG_TO_USER);
        }
        reservation.setReservationStatus(reservationstatusRequest.reservationStatus());
        reservationRepository.save(reservation);
        return getReservationResponse(reservation);
    }

    private static void addCancelReasonAndDetail(Reservation canceledReservation,
        CancelReason cancelReason, String cancelDetail) {
        canceledReservation.setCancelReason(cancelReason);
        canceledReservation.setCancelDetail(cancelDetail);
        canceledReservation.setReservationStatus(ReservationStatus.CANCEL);
    }

    private static ReservationResponse getReservationResponse(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getManager().getId(),
            reservation.getDepartureLocation(),
            reservation.getArrivalLocation(),
            reservation.getReservationDateTime(),
            reservation.getServiceType(),
            reservation.getTransportation(),
            reservation.getPrice(),
            reservation.getReservationStatus(),
            getPatientResponse(reservation)
        );
    }

    private static PatientResponse getPatientResponse(Reservation reservation) {
        return new PatientResponse(reservation.getPatient().getName(),
            reservation.getPatient().getPhoneNumber(),
            reservation.getPatient().getGender(),
            reservation.getPatient().getPatientRelation(),
            reservation.getPatient().getBirthDate().toString(),
            reservation.getPatient().getNokPhone());
    }

    private static Reservation getReservation(ReservationRequest reservationRequest, User user,
        LocalDateTime reservationDateTime, Patient patient, Manager manager) {
        return Reservation.builder()
            .departureLocation(reservationRequest.departureLocation())
            .arrivalLocation(reservationRequest.arrivalLocation())
            .reservationDateTime(reservationDateTime)
            .serviceType(reservationRequest.serviceType())
            .transportation(reservationRequest.transportation())
            .price(reservationRequest.price())
            .createdTime(LocalDateTime.now())
            .reservationStatus(ReservationStatus.HOLD)
            .patient(patient)
            .manager(manager)
            .user(user)
            .build();
    }

    private static LocalDateTime getLocalDateTime(ReservationRequest reservationRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.parse(reservationRequest.reservationDateTime(), formatter);
    }

    private static void validateReservationRequest(ReservationRequest reservationRequest) {
        if (!Arrays.stream(ServiceType.values()).toList()
            .contains(reservationRequest.serviceType())) {
            throw new ReservationException(INVALID_SERVICE_TYPE);
        }
        if (!Arrays.stream(Transportation.values()).toList()
            .contains(reservationRequest.transportation())) {
            throw new ReservationException(INVALID_TRANSPORTATION_TYPE);
        }
        if (reservationRequest.managerId() == null) {
            throw new ReservationException(MANAGER_REQUIRED);
        }
        if(reservationRequest.departureLocation().isEmpty() ){
            throw new ReservationException(INVALID_DEPARTRUE_ADDRESS);
        }
        if(reservationRequest.arrivalLocation().isEmpty()) {
            throw new ReservationException(INVALID_ARRIVAL_ADDRESS);
        }
    }
}
