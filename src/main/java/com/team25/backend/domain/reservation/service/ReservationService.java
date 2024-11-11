package com.team25.backend.domain.reservation.service;

import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_ARRIVAL_ADDRESS;
import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_CANCEL_REASON;
import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_DATETIME_FORMAT;
import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_DEPARTRUE_ADDRESS;
import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_NOK_PHONE;
import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_PATIENT_BIRTHDATE;
import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_PATIENT_GENDER;
import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_PATIENT_NAME;
import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_PATIENT_PHONE;
import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_PATIENT_RELATION;
import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_PRICE;
import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_SERVICE_TYPE;
import static com.team25.backend.global.exception.ReservationErrorCode.INVALID_TRANSPORTATION_TYPE;
import static com.team25.backend.global.exception.ReservationErrorCode.MANAGER_NOT_FOUND;
import static com.team25.backend.global.exception.ReservationErrorCode.NOT_MANAGER;
import static com.team25.backend.global.exception.ReservationErrorCode.PATIENT_REQUIRED;
import static com.team25.backend.global.exception.ReservationErrorCode.RESERVATION_ALREADY_CANCELED;
import static com.team25.backend.global.exception.ReservationErrorCode.RESERVATION_NOT_BELONG_TO_USER;
import static com.team25.backend.global.exception.ReservationErrorCode.RESERVATION_NOT_FOUND;
import static com.team25.backend.global.exception.ReservationErrorCode.USER_NOT_FOUND;

import com.team25.backend.domain.manager.entity.Manager;
import com.team25.backend.domain.manager.repository.ManagerRepository;
import com.team25.backend.domain.patient.dto.request.PatientRequest;
import com.team25.backend.domain.patient.dto.response.PatientResponse;
import com.team25.backend.domain.patient.entity.Patient;
import com.team25.backend.domain.patient.enumdomain.PatientGender;
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
import jakarta.validation.constraints.NotNull;
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
        validateCancelReason(cancelRequest);
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
            cancelRequest.cancelDetail());
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
        List<Reservation> byUserUuid = reservationRepository.findByUser_Uuid(user.getUuid());
        if(byUserUuid.isEmpty()){
            throw new ReservationException(RESERVATION_NOT_FOUND);
        }
        for (Reservation reservation : byUserUuid) {
            if(reservation.getId().equals(reservationId)) {
                reservation.setReservationStatus(reservationstatusRequest.reservationStatus());
                reservationRepository.save(reservation);
                return getReservationResponse(reservation);
            }
        }
        throw new ReservationException(RESERVATION_NOT_BELONG_TO_USER);
    }

    public List<ReservationResponse> getManagerReservation(User user) { // ManagerUser
        if(!user.getRole().equals("ROLE_MANAGER")){
            throw new ReservationException(NOT_MANAGER);
        }
        if (user.getManager() == null) {
            throw new ReservationException(MANAGER_NOT_FOUND);
        }
        List<Reservation> allReservation = reservationRepository.findByManager_Id(user.getManager().getId());
        if(allReservation.isEmpty()){
            throw new ReservationException(RESERVATION_NOT_FOUND);
        }
        List<ReservationResponse> responseList = new ArrayList<>();
        for (Reservation reservation : allReservation) {
            if(!reservation.getReservationStatus().equals(ReservationStatus.CANCEL)){
                responseList.add(getReservationResponse(reservation));
            }
        }
        return responseList;
    }

    private static void validateCancelReason(CancelRequest cancelRequest) {
        if(cancelRequest.cancelDetail().isEmpty()){
            throw new ReservationException(ReservationErrorCode.CANCEL_REASON_REQUIRED);
        }
        if (cancelRequest.cancelReason() == null) {
            throw new ReservationException(ReservationErrorCode.CANCEL_REASON_REQUIRED);
        }
        if(!Arrays.stream(CancelReason.values()).toList().contains(cancelRequest.cancelReason())){
            throw new ReservationException(INVALID_CANCEL_REASON);
        }
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
            .reports(new ArrayList<>())
            .build();
    }

    private static LocalDateTime getLocalDateTime(ReservationRequest reservationRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.parse(reservationRequest.reservationDateTime(), formatter);
    }

   public static void validateReservationRequest(ReservationRequest request) {
       validateLocations(request.departureLocation(), request.arrivalLocation());
       validateDateTime(request.reservationDateTime());
       validateTypes(request.serviceType(), request.transportation());
       validatePrice(request.price());
       validatePatient(request.patient());
   }


   private static void validateLocations(String departure, String arrival) {
       if (departure == null || departure.isEmpty() || departure.isBlank()) {
           throw new ReservationException(INVALID_DEPARTRUE_ADDRESS);
       }
       if (arrival == null || arrival.isEmpty() || arrival.isBlank()) {
           throw new ReservationException(INVALID_ARRIVAL_ADDRESS);
       }
   }

   private static void validateDateTime(String dateTime) {
       if (dateTime == null || dateTime.isEmpty()) {
           throw new ReservationException(INVALID_DATETIME_FORMAT);
       }
       try {
           if (!dateTime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
               throw new ReservationException(INVALID_DATETIME_FORMAT);
           }
       } catch (Exception e) {
           throw new ReservationException(INVALID_DATETIME_FORMAT);
       }
   }

   private static void validateTypes(ServiceType serviceType, Transportation transportation) {
       if (serviceType == null) {
           throw new ReservationException(INVALID_SERVICE_TYPE);
       }
       if (!Arrays.stream(ServiceType.values()).toList().contains(serviceType)) {
           throw new ReservationException(INVALID_SERVICE_TYPE);
       }

       if (transportation == null) {
           throw new ReservationException(INVALID_TRANSPORTATION_TYPE);
       }
       if (!Arrays.stream(Transportation.values()).toList().contains(transportation)) {
           throw new ReservationException(INVALID_TRANSPORTATION_TYPE);
       }
   }

   private static void validatePrice(int price) {
       if (price < 0) {
           throw new ReservationException(INVALID_PRICE);
       }
   }

   private static void validatePatient(@NotNull PatientRequest patient) {
       if (patient == null) {
           throw new ReservationException(PATIENT_REQUIRED);
       }
       validatePatientFields(patient);
   }

   private static void validatePatientFields(PatientRequest patient) {
       if (patient.name() == null || patient.name().isEmpty()) {
           throw new ReservationException(INVALID_PATIENT_NAME);
       }
       if (patient.phoneNumber() == null || !patient.phoneNumber().matches("\\d{3}-\\d{4}-\\d{4}")) {
           throw new ReservationException(INVALID_PATIENT_PHONE);
       }
       if (patient.patientGender() == null || !Arrays.stream(PatientGender.values()).toList().contains(patient.patientGender())) {
           throw new ReservationException(INVALID_PATIENT_GENDER);
       }
       if (patient.patientRelation() == null || patient.patientRelation().isEmpty()) {
           throw new ReservationException(INVALID_PATIENT_RELATION);
       }
       if (patient.birthDate() == null || !patient.birthDate().matches("\\d{4}-\\d{2}-\\d{2}")) {
           throw new ReservationException(INVALID_PATIENT_BIRTHDATE);
       }
       if (patient.nokPhone() == null || !patient.nokPhone().matches("\\d{3}-\\d{4}-\\d{4}")) {
           throw new ReservationException(INVALID_NOK_PHONE);
       }
   }
}
