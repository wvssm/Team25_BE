package com.team25.backend.domain.reservation.service;

import static com.team25.backend.global.exception.ErrorCode.CANCEL_REASON_REQUIRED;
import static com.team25.backend.global.exception.ErrorCode.INVALID_ARRIVAL_ADDRESS;
import static com.team25.backend.global.exception.ErrorCode.INVALID_DATETIME_FORMAT;
import static com.team25.backend.global.exception.ErrorCode.INVALID_DEPARTRUE_ADDRESS;
import static com.team25.backend.global.exception.ErrorCode.INVALID_NOK_PHONE;
import static com.team25.backend.global.exception.ErrorCode.INVALID_PATIENT_BIRTHDATE;
import static com.team25.backend.global.exception.ErrorCode.INVALID_PATIENT_GENDER;
import static com.team25.backend.global.exception.ErrorCode.INVALID_PATIENT_NAME;
import static com.team25.backend.global.exception.ErrorCode.INVALID_PATIENT_PHONE;
import static com.team25.backend.global.exception.ErrorCode.INVALID_PATIENT_RELATION;
import static com.team25.backend.global.exception.ErrorCode.INVALID_PRICE;
import static com.team25.backend.global.exception.ErrorCode.INVALID_RESERVATION_STATUS;
import static com.team25.backend.global.exception.ErrorCode.INVALID_SERVICE_TYPE;
import static com.team25.backend.global.exception.ErrorCode.INVALID_TRANSPORTATION_TYPE;
import static com.team25.backend.global.exception.ErrorCode.MANAGER_NOT_FOUND;
import static com.team25.backend.global.exception.ErrorCode.NOT_MANAGER;
import static com.team25.backend.global.exception.ErrorCode.RESERVATION_ALREADY_CANCELED;
import static com.team25.backend.global.exception.ErrorCode.RESERVATION_NOT_BELONG_TO_USER;
import static com.team25.backend.global.exception.ErrorCode.RESERVATION_NOT_FOUND;
import static com.team25.backend.global.exception.ErrorCode.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team25.backend.domain.manager.dto.request.ManagerCreateRequest;
import com.team25.backend.domain.manager.dto.response.ManagerCreateResponse;
import com.team25.backend.domain.manager.entity.Manager;
import com.team25.backend.domain.manager.repository.CertificateRepository;
import com.team25.backend.domain.manager.repository.ManagerRepository;
import com.team25.backend.domain.manager.repository.WorkingHourRepository;
import com.team25.backend.domain.manager.service.ManagerService;
import com.team25.backend.domain.patient.dto.request.PatientRequest;
import com.team25.backend.domain.patient.entity.Patient;
import com.team25.backend.domain.patient.enumdomain.PatientGender;
import com.team25.backend.domain.patient.repository.PatientRepository;
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
import com.team25.backend.domain.user.repository.UserRepository;
import com.team25.backend.domain.user.service.UserService;
import com.team25.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.ANY)
class ReservationServiceTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    ManagerRepository managerRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReservationService reservationService;

    private User user;
    private Reservation reservation;
    private Reservation newReservation;
    private final String UserUUID = "uuid";
    private PatientRequest patientRequest;
    private ReservationRequest reservationRequest;
    private ReservationRequest newReservationRequest;
    private User newUser;
    private ObjectMapper objectMapper;
    @Autowired
    private ManagerService managerService;
    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private WorkingHourRepository workingHourRepository;
    @Autowired
    private UserService userService;


    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        certificateRepository.deleteAll();
        workingHourRepository.deleteAll();
        managerRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();
        user = userRepository.save(new User("userName", UserUUID, "ROLE_USER"));
        newUser = userRepository.save(
            new User("userName2_MANAGER", UserUUID + "ABCDEFGH", "ROLE_MANAGER"));
        managerService.createManager(newUser,
            new ManagerCreateRequest("managerName", "a", "A", "a", "a", "a"));
        Long firstManager = managerRepository.findAll().getFirst().getId();
        Patient patient = patientRepository.save(new Patient(1L, "patientName", "010-0000-0000",
            PatientGender.MALE, LocalDate.now(), "000-0000-0000", "relation", null));
        reservation = new Reservation(null, null, user, "department", "arrival",
            LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.HOLD, null, null,
            LocalDateTime.now(),
            ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000, false, null,
            null, patient);
        newReservation = new Reservation(null, null, user, "department", "arrival",
            LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.HOLD, null, null,
            LocalDateTime.now(),
            ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 20000, false, null,
            null, patient);
        patientRequest = new PatientRequest(
            "patientName", "010-0000-0000", PatientGender.MALE, "relation", "1999-01-01",
            "010-0000-0000");
        reservationRequest = new ReservationRequest(firstManager, "departure", "arrival",
            "2024-05-01 15:25",
            ServiceType.CLINIC_ESCORT,
            Transportation.PUBLIC_TRANSPORTATION, 10000, patientRequest);
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        certificateRepository.deleteAll();
        workingHourRepository.deleteAll();
        patientRepository.deleteAll();
        managerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("예약 생성 테스트")
    void createReservation() {
        // given
        patientRequest = new PatientRequest(
            "patientName", "010-0000-0000", PatientGender.MALE, "relation", "1999-01-01",
            "010-0000-0000");
        reservationRequest = new ReservationRequest(newUser.getManager().getId(), "department",
            "arrival",
            "2024-05-01 15:25",
            ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 20000, patientRequest);

        // when
        ReservationResponse createdReservation = reservationService.createReservation(
            reservationRequest,
            user);
        Reservation savedReservation = reservationRepository.findById(
            createdReservation.reservationId()).orElseThrow();

        // then
        assertThat(reservationRepository.findAll()).hasSize(1);
        assertThat(createdReservation).isNotNull();
        assertThat(createdReservation).satisfies(
            reservationResponse -> {
                assertThat(reservationResponse.departureLocation()).isEqualTo("department");
                assertThat(reservationResponse.arrivalLocation()).isEqualTo("arrival");
                assertThat(reservationResponse.serviceType()).isEqualTo(ServiceType.CLINIC_ESCORT);
                assertThat(reservationResponse.transportation()).isEqualTo(
                    Transportation.PUBLIC_TRANSPORTATION);
                assertThat(reservationResponse.reservationStatus()).isEqualTo(
                    ReservationStatus.HOLD);
                assertThat(reservationResponse.price()).isEqualTo(20000);
            }
        );

        assertThat(savedReservation).isNotNull();
        assertThat(savedReservation.getUser()).isNotNull();
        assertThat(savedReservation.getUser()).isEqualTo(user);
        assertThat(createdReservation).satisfies(
            reservationResponse -> {
                assertThat(reservationResponse.patient().name()).isEqualTo("patientName");
                assertThat(reservationResponse.patient().patientGender()).isEqualTo(
                    PatientGender.MALE);
                assertThat(reservationResponse.patient().phoneNumber()).isEqualTo(
                    "010-0000-0000".replace("-", ""));
                assertThat(reservationResponse.patient().nokPhone()).isEqualTo(
                    "010-0000-0000".replace("-", ""));
                assertThat(reservationResponse.patient().birthDate()).isEqualTo("1999-01-01");
            }
        );
    }

    @Test
    @DisplayName("전체 예약 조회 테스트")
    void getAllReservations() {
        // given
        patientRequest = new PatientRequest(
            "patientName", "010-0000-0000", PatientGender.MALE, "relation", "1999-01-01",
            "010-0000-0000");
        Long managerId = managerRepository.findAll().getFirst().getId();
        reservationRequest = new ReservationRequest(managerId, "department", "arrival",
            "2024-05-01 15:25",
            ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000, patientRequest);
        newReservationRequest = new ReservationRequest(managerId, "department", "arrival",
            "2024-05-01 15:25",
            ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 20000, patientRequest);

        reservationService.createReservation(reservationRequest, user);
        reservationService.createReservation(newReservationRequest, user);

        // when
        List<ReservationResponse> allReservations = reservationService.getAllReservations(user);

        // then
        assertThat(allReservations).isNotNull();
        assertThat(allReservations).hasSize(2);
        assertThat(allReservations).satisfies(
            reservationResponseList -> {
                for (ReservationResponse reservationResponse : reservationResponseList) {
                    assertThat(reservationResponse.departureLocation()).isEqualTo("department");
                    assertThat(reservationResponse.arrivalLocation()).isEqualTo("arrival");
                    assertThat(reservationResponse.serviceType()).isEqualTo(
                        ServiceType.CLINIC_ESCORT);
                    assertThat(reservationResponse.transportation()).isEqualTo(
                        Transportation.PUBLIC_TRANSPORTATION);
                    assertThat(reservationResponse.reservationStatus()).isEqualTo(
                        ReservationStatus.HOLD);
                    assertThat(reservationResponse.patient().name()).isEqualTo("patientName");
                    assertThat(reservationResponse.patient().patientGender()).isEqualTo(
                        PatientGender.MALE);
                    assertThat(reservationResponse.patient().phoneNumber()).isEqualTo(
                        "010-0000-0000".replace("-", ""));
                    assertThat(reservationResponse.patient().nokPhone()).isEqualTo(
                        "010-0000-0000".replace("-", ""));
                    assertThat(reservationResponse.patient().birthDate()).isEqualTo("1999-01-01");
                }
                assertThat(reservationResponseList.getFirst().price()).isEqualTo(10000);
                assertThat(reservationResponseList.getLast().price()).isEqualTo(20000);
            }
        );
    }

    @Test
    @DisplayName("예약이 존재하지 않을 때 전체 조회")
    void getAllReservationsWithEmptyList() {
        // when & then
        assertThatThrownBy(() -> reservationService.getAllReservations(user)).isInstanceOf(
            CustomException.class);

        assertThatThrownBy(() -> reservationService.getAllReservations(user)).hasMessage(
            "예약이 존재하지 않습니다.");

        assertThatThrownBy(() -> reservationService.getAllReservations(user)).hasNoCause();
    }

    @Test
    @DisplayName("예약 ID로 단일 예약 조회")
    void getReservationById() {
        // given
        patientRequest = new PatientRequest(
            "patientName", "010-0000-0000", PatientGender.MALE, "relation", "1999-01-01",
            "010-0000-0000");
        Long managerId = managerRepository.findAll().getFirst().getId();
        reservationRequest = new ReservationRequest(managerId, "department", "arrival",
            "2024-05-01 15:25",
            ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000, patientRequest);
        newReservationRequest = new ReservationRequest(managerId, "department", "arrival",
            "2024-05-01 15:25",
            ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 20000, patientRequest);

        // when
        ReservationResponse reservation1 = reservationService.createReservation(reservationRequest,
            user);
        reservationService.createReservation(
            newReservationRequest, user);
        ReservationResponse reservationById = reservationService.getReservationById(user,
            reservation1.reservationId());

        // then
        assertThat(reservationById).isNotNull();
        assertThat(reservationById).isInstanceOf(ReservationResponse.class);
        assertThat(reservationById).satisfies(
            reservationResponse -> {
                assertThat(reservationResponse.departureLocation()).isEqualTo("department");
                assertThat(reservationResponse.arrivalLocation()).isEqualTo("arrival");
                assertThat(reservationResponse.serviceType()).isEqualTo(ServiceType.CLINIC_ESCORT);
                assertThat(reservationResponse.transportation()).isEqualTo(
                    Transportation.PUBLIC_TRANSPORTATION);
                assertThat(reservationResponse.reservationStatus()).isEqualTo(
                    ReservationStatus.HOLD);
                assertThat(reservationResponse.patient().name()).isEqualTo("patientName");
                assertThat(reservationResponse.patient().patientGender()).isEqualTo(
                    PatientGender.MALE);
                assertThat(reservationResponse.patient().phoneNumber()).isEqualTo(
                    "010-0000-0000".replace("-", ""));
                assertThat(reservationResponse.patient().nokPhone()).isEqualTo(
                    "010-0000-0000".replace("-", ""));
                assertThat(reservationResponse.patient().birthDate()).isEqualTo("1999-01-01");
                assertThat(reservationResponse.price()).isEqualTo(10000);
                assertThat(reservationResponse.managerId()).isEqualTo(managerId);
                assertThat(reservationResponse.reservationDateTime()).isEqualTo("2024-05-01T15:25");
            }
        );
    }

    @Test
    @DisplayName("잘못된 유저 UUID로 인한 단일 예약 조회 실패 테스트")
    public void getREservationByID_WithWrongUserUUIDTest() throws Exception {
        // given
        String wrongUserUUID = user.getUuid() + UUID.randomUUID().toString();
        User savedUser = userRepository.save(
            new User(162L, "wrongUUIDUser", wrongUserUUID, "USER_ROLE", null, new ArrayList<>()));
        reservationRepository.save(reservation);

        // when & then
        assertThatThrownBy(
            () -> reservationService.getReservationById(savedUser, reservation.getId()))
            .isInstanceOf(CustomException.class)
            .satisfies(
                exception -> {
                    CustomException ex = (CustomException) exception;
                    assertThat(ex.getErrorCode()).isEqualTo(USER_NOT_FOUND);
                    assertThat(ex.getMessage()).isEqualTo(USER_NOT_FOUND.getMessage());
                }
            );
    }

    @Test
    @DisplayName("다른 사용자의 예약 ID로 인한 단일 예약 조회 실패 테스트")
    public void getReservationById_WithAnotherUsersReservationIdTest() throws Exception {
        // given
        reservationRequest = new ReservationRequest(newUser.getManager().getId(),
            "departure",
            "arrival",
            "2024-05-11 15:25",
            ServiceType.CLINIC_ESCORT,
            Transportation.PUBLIC_TRANSPORTATION,
            10000,
            patientRequest
        );
        ReservationResponse savedReservationResponse = reservationService.createReservation(
            reservationRequest, newUser);
        reservationService.createReservation(
            new ReservationRequest(newUser.getManager().getId(), "a", "a", "2024-11-11 12:52"
                , ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000,
                patientRequest), user);

        // when & then
        assertThatThrownBy(
            () -> reservationService.getReservationById(user,
                savedReservationResponse.reservationId()))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(RESERVATION_NOT_BELONG_TO_USER);
                assertThat(ex.getMessage()).isEqualTo(RESERVATION_NOT_BELONG_TO_USER.getMessage());
            });
    }

    @Test
    @DisplayName("잘못된 ID로 단일 예약 조회")
    void getReservationByWrongId() {
        // given
        Long id = managerRepository.findAll().getFirst().getId();
        ReservationResponse savedReservation = reservationService.createReservation(
            new ReservationRequest(id, "location", "location", "2024-11-15 15:24",
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 100000,
                patientRequest), user);
        Random random = new Random();

        // when & then
        assertThatThrownBy(() -> reservationService.getReservationById(user,
            random.nextLong(savedReservation.reservationId(), 50000L)))
            .isInstanceOf(CustomException.class)
            .hasMessage(RESERVATION_NOT_FOUND.getMessage())
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(RESERVATION_NOT_FOUND);
                assertThat(ex.getMessage()).isEqualTo(RESERVATION_NOT_FOUND.getMessage());
            });
    }

    @Test
    @DisplayName("예약 Request 출발지 Validation 테스트")
    void validateReservationRequestLocationTest() {
        // when & then
        assertThatThrownBy(() -> ReservationService.validateReservationRequest(
            new ReservationRequest(1L, "", "arrival", "2024-05-01 15:25", ServiceType.CLINIC_ESCORT,
                Transportation.PUBLIC_TRANSPORTATION, 10000, patientRequest)
        )).isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_DEPARTRUE_ADDRESS);
                assertThat(ex.getMessage()).isEqualTo(INVALID_DEPARTRUE_ADDRESS.getMessage());

            });
    }

    @Test
    @DisplayName("예약 Request 도착지 Validation 테스트")
    void validateReservationRequestArrivalLocatoinTest() {
        // when & then
        assertThatThrownBy(() -> ReservationService.validateReservationRequest(
            new ReservationRequest(1L, "departure", "", "2024-05-01 15:25",
                ServiceType.CLINIC_ESCORT,
                Transportation.PUBLIC_TRANSPORTATION, 10000, patientRequest)
        )).isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_ARRIVAL_ADDRESS);
                assertThat(ex.getMessage()).isEqualTo(INVALID_ARRIVAL_ADDRESS.getMessage());
            });
    }

    @Test
    @DisplayName("예약 Request 날짜 형식 Validation 테스트")
    void validateReservationDateTest() {
        // when & then
        assertThatThrownBy(() -> ReservationService.validateReservationRequest(
            new ReservationRequest(1L, "departure", "arrival", "2024-05-01 15:25:00",
                ServiceType.CLINIC_ESCORT,
                Transportation.PUBLIC_TRANSPORTATION, 10000, patientRequest)
        )).isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_DATETIME_FORMAT);
            })
            .hasMessage(INVALID_DATETIME_FORMAT.getMessage());
        // when & then
        assertThatThrownBy(() -> ReservationService.validateReservationRequest(
            new ReservationRequest(1L, "departure", "arrival", "05-01 15:25",
                ServiceType.CLINIC_ESCORT,
                Transportation.PUBLIC_TRANSPORTATION, 10000, patientRequest)
        )).isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_DATETIME_FORMAT);
                assertThat(ex.getMessage()).isEqualTo(INVALID_DATETIME_FORMAT.getMessage());
            });
    }


    @Test
    @DisplayName("예약 Request 서비스 타입 Validation 테스트")
    void validateReservationRequestServiceTypeTest() {
        // given
        ObjectMapper objectMapper = new ObjectMapper();

        // when & then
        assertThatThrownBy(() -> objectMapper.readValue("\"없는서비스타입\"", ServiceType.class))
            .isInstanceOf(JsonMappingException.class);

        assertThatThrownBy(() -> objectMapper.readValue("\"\"", ServiceType.class))
            .isInstanceOf(JsonMappingException.class);

        assertThatThrownBy(() ->
            ReservationService.validateReservationRequest(
                new ReservationRequest(1L, "departure", "arrival", "2024-05-01 15:25",
                    null, Transportation.PUBLIC_TRANSPORTATION, 10000, patientRequest)))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_SERVICE_TYPE);
                assertThat(ex.getMessage()).isEqualTo(INVALID_SERVICE_TYPE.getMessage());
            });
    }

    @Test
    @DisplayName("예약 Request 이동수단 타입 VAlidation 테스트")
    void validateReservationRequestTransportationTest() {
        // given
        ObjectMapper objectMapper = new ObjectMapper();

        // when & then
        assertThatThrownBy(() -> objectMapper.readValue("\"없는이동수단\"", Transportation.class))
            .isInstanceOf(JsonMappingException.class);

        assertThatThrownBy(() -> objectMapper.readValue("\"\"", Transportation.class))
            .isInstanceOf(JsonMappingException.class);

        assertThatThrownBy(() ->
            ReservationService.validateReservationRequest(
                new ReservationRequest(1L, "departure", "arrival", "2024-05-01 15:25",
                    ServiceType.CLINIC_ESCORT, null, 10000, patientRequest)))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_TRANSPORTATION_TYPE);
                assertThat(ex.getMessage()).isEqualTo(INVALID_TRANSPORTATION_TYPE.getMessage());
            });
    }

    @Test
    @DisplayName("예약 Request 가격 Validation 테스트")
    void validateReservationRequestPriceTest() {
        assertThatThrownBy(() -> ReservationService.validateReservationRequest(
            new ReservationRequest(1L, "departure", "arrival", "2024-05-01 15:25",
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, -125,
                patientRequest)))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_PRICE);
                assertThat(ex.getMessage()).isEqualTo(INVALID_PRICE.getMessage());
            });

    }

    @Test
    @DisplayName("환자 정보 검증 이름 누락 케이스 테스트")
    void validateReservationRequestPatientReqeustTest() {
        // given
        patientRequest = new PatientRequest(
            null, "010-0000-0000", PatientGender.MALE, "relation", "1999-01-01",
            "010-0000-0000");

        // when & then
        assertThatThrownBy(() -> ReservationService.validateReservationRequest(
            new ReservationRequest(1L, "departure", "arrival", "2024-05-01 15:25",
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000,
                patientRequest
            )))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_PATIENT_NAME);
                assertThat(ex.getMessage()).isEqualTo(INVALID_PATIENT_NAME.getMessage());
            });
    }

    @Test
    @DisplayName("환자 정보 검증 전화번호 누락 케이스 테스트")
    void validateReservationRequestPatientReqeustWithoutPhoneNumberTest() {
        // given
        patientRequest = new PatientRequest(
            "patientName", null, PatientGender.MALE, "relation", "1999-01-01",
            "010-0000-0000");

        // when & then
        assertThatThrownBy(() -> ReservationService.validateReservationRequest(
            new ReservationRequest(1L, "departure", "arrival", "2024-05-01 15:25",
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000,
                patientRequest
            )))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_PATIENT_PHONE);
                assertThat(ex.getMessage()).isEqualTo(INVALID_PATIENT_PHONE.getMessage());
            });
    }

    @Test
    @DisplayName("환자 정보 검증 성별 누락 케이스 테스트")
    void validateReservationRequestPatientReqeustWithoutGenderTest() {
        // given
        patientRequest = new PatientRequest(
            "patientName", "010-0000-0000", null, "relation", "1999-01-01",
            "010-0000-0000");
        ObjectMapper objectMapper = new ObjectMapper();

        // when & then

        assertThatThrownBy(() -> objectMapper.readValue("\"없는성별\"", PatientGender.class))
            .isInstanceOf(JsonMappingException.class);

        assertThatThrownBy(() -> ReservationService.validateReservationRequest(
            new ReservationRequest(1L, "departure", "arrival", "2024-05-01 15:25",
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000,
                patientRequest
            )))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_PATIENT_GENDER);
                assertThat(ex.getMessage()).isEqualTo(INVALID_PATIENT_GENDER.getMessage());
            });
    }

    @Test
    @DisplayName("환자 정보 검증 관계 누락 케이스 테스트")
    void validateReservationRequestPatientReqeustWithoutRelationTest() {
        // given
        patientRequest = new PatientRequest(
            "patientName", "010-0000-0000", PatientGender.MALE, "", "1999-01-01",
            "010-0000-0000");

        // when & then
        assertThatThrownBy(() -> ReservationService.validateReservationRequest(
            new ReservationRequest(1L, "departure", "arrival", "2024-05-01 15:25",
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000,
                patientRequest
            )))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_PATIENT_RELATION);
                assertThat(ex.getMessage()).isEqualTo(INVALID_PATIENT_RELATION.getMessage());
            });
    }

    @Test
    @DisplayName("환자 정보 검증 생년월일 누락 케이스 테스트")
    void validateReservationRequestPatientReqeustWithoutBirthDateTest() {
        // given
        patientRequest = new PatientRequest(
            "patientName", "010-0000-0000", PatientGender.MALE, "relation", "1999-01-12 15:11:11",
            "010-0000-0000");

        PatientRequest patientRequest1 = new PatientRequest(
            "patientName", "010-0000-0000", PatientGender.MALE, "relation", "1999-01",
            "010-0000-0000");

        PatientRequest patientRequest2 = new PatientRequest(
            "patientName", "010-0000-0000", PatientGender.MALE, "relation", "",
            "010-0000-0000");

        // when & then
        assertThatThrownBy(() -> ReservationService.validateReservationRequest(
            new ReservationRequest(1L, "departure", "arrival", "2024-05-01 15:25",
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000,
                patientRequest
            )))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_PATIENT_BIRTHDATE);
                assertThat(ex.getMessage()).isEqualTo(INVALID_PATIENT_BIRTHDATE.getMessage());
            });
        assertThatThrownBy(() -> ReservationService.validateReservationRequest(
            new ReservationRequest(1L, "departure", "arrival", "2024-05-01 15:25",
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000,
                patientRequest1
            )))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_PATIENT_BIRTHDATE);
                assertThat(ex.getMessage()).isEqualTo(INVALID_PATIENT_BIRTHDATE.getMessage());
            });
        assertThatThrownBy(() -> ReservationService.validateReservationRequest(
            new ReservationRequest(1L, "departure", "arrival", "2024-05-01 15:25",
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000,
                patientRequest2
            )))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_PATIENT_BIRTHDATE);
                assertThat(ex.getMessage()).isEqualTo(INVALID_PATIENT_BIRTHDATE.getMessage());
            });
    }

    @Test
    @DisplayName("환자 정보 검증 보호자 번호 누락 케이스 테스트")
    void validateReservationRequestPatientReqeustWithoutNokPhoneNumberTest() {
        // given
        patientRequest = new PatientRequest(
            "patientName", "010-0000-0000", PatientGender.MALE, "relation", "1999-01-01",
            null);

        PatientRequest patientRequest1 = new PatientRequest(
            "patientName", "010-0000-0000", PatientGender.MALE, "relation", "1999-01-01",
            "010-0000-00000");

        PatientRequest patientRequest2 = new PatientRequest(
            "patientName", "010-0000-0000", PatientGender.MALE, "relation", "1999-01-01",
            "010-000-00000");

        // when & then
        assertThatThrownBy(() -> ReservationService.validateReservationRequest(
            new ReservationRequest(1L, "departure", "arrival", "2024-05-01 15:25",
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000,
                patientRequest
            )))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_NOK_PHONE);
                assertThat(ex.getMessage()).isEqualTo(INVALID_NOK_PHONE.getMessage());
            });

        assertThatThrownBy(() -> ReservationService.validateReservationRequest(
            new ReservationRequest(1L, "departure", "arrival", "2024-05-01 15:25",
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000,
                patientRequest1
            )))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_NOK_PHONE);
                assertThat(ex.getMessage()).isEqualTo(INVALID_NOK_PHONE.getMessage());
            });

        assertThatThrownBy(() -> ReservationService.validateReservationRequest(
            new ReservationRequest(1L, "departure", "arrival", "2024-05-01 15:25",
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000,
                patientRequest2
            )))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_NOK_PHONE);
                assertThat(ex.getMessage()).isEqualTo(INVALID_NOK_PHONE.getMessage());
            });
    }

    @Test
    @DisplayName("예약 취소 테스트")
    void cancelReservationTest() {
        // given
        reservationService.createReservation(reservationRequest, user);
        CancelRequest cancelRequest = new CancelRequest(CancelReason.PATIENT_CANCEL,
            "CancelDetail");
        ReservationResponse reservation1 = reservationService.createReservation(reservationRequest,
            user);

        // when
        ReservationResponse cancelledReservation = reservationService.cancelReservation(user,
            cancelRequest, reservation1.reservationId());
        Reservation savedReservation = reservationRepository.findById(
            cancelledReservation.reservationId()).orElseThrow();

        // then
        assertThat(cancelledReservation).isNotNull();
        assertThat(cancelledReservation).satisfies(
            reservationResponse -> {
                assertThat(
                    reservationResponse.reservationStatus()).isEqualTo(ReservationStatus.CANCEL);
                assertThat(savedReservation.getCancelReason()).isEqualTo(
                    CancelReason.PATIENT_CANCEL);
                assertThat(savedReservation.getCancelDetail()).isEqualTo("CancelDetail");
            }
        );
    }

    @Test
    @DisplayName("예약 취소 잘못된 취소 이유값 테스트")
    void cancelReservationWithWrongCancelReasonTest() {
        // given
        patientRequest = new PatientRequest(
            "patientName", "010-0000-0000", PatientGender.MALE, "relation", "1999-01-01",
            "010-0000-0000");
        reservationRequest = new ReservationRequest(managerRepository.findAll().getFirst().getId(),
            "department", "arrival",
            "2024-05-01 15:25", ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION,
            20000, patientRequest);
        reservationService.createReservation(reservationRequest, user);
        CancelRequest cancelRequest = new CancelRequest(null, "CancelDetail");
        objectMapper = new ObjectMapper();

        // when & then
        assertThatThrownBy(() -> objectMapper.readValue("\"\"", CancelReason.class))
            .isInstanceOf(JsonMappingException.class);
        assertThatThrownBy(() -> objectMapper.readValue("\"없는취소 이유\"", CancelReason.class))
            .isInstanceOf(JsonMappingException.class);

        assertThatThrownBy(() -> reservationService.cancelReservation(user, cancelRequest, 1L))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(CANCEL_REASON_REQUIRED);
                assertThat(ex.getMessage()).isEqualTo(CANCEL_REASON_REQUIRED.getMessage());
            });

    }

    @Test
    @DisplayName("잘못된 유저 정보로 인한 예약 취소 실패 테스트")
    public void cancelReservationWithWrongUserInfoTest() throws Exception {
        // given
        String wrongUserUUID = UUID.randomUUID().toString();
        User forWrongUUIDUser = new User("name", wrongUserUUID, "ROLE_USER");

        // when & then
        assertThat(reservationRepository.findByUser_Uuid(forWrongUUIDUser.getUuid())).hasSize(0);
        assertThatThrownBy(() -> reservationService.cancelReservation(forWrongUUIDUser,
            new CancelRequest(CancelReason.PATIENT_CANCEL, "detail"), reservation.getId()))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(USER_NOT_FOUND);
                assertThat(ex.getMessage()).isEqualTo(USER_NOT_FOUND.getMessage());
            });

    }

    @Test
    @DisplayName("이미 취소된 예약 취소 실패 테스트")
    public void cancelReservationAlreadyCanceledTest() throws Exception {
        // given
        Manager testManager = managerRepository.findAll().getFirst();
        reservation.setManager(testManager);
        reservationRepository.save(reservation);
        reservationService.cancelReservation(user,
            new CancelRequest(CancelReason.PATIENT_CANCEL, "Detail"),
            reservation.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.cancelReservation(user,
            new CancelRequest(CancelReason.CAREGIVER_CANCEL, "deatilFailure"),
            reservation.getId()))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(RESERVATION_ALREADY_CANCELED);
                assertThat(ex.getMessage()).isEqualTo(RESERVATION_ALREADY_CANCELED.getMessage());
            });
    }


    @Test
    @DisplayName("예약 상태 변경 테스트")
    void changeReservationStatus() {
        // given
        // 매니저 생성 요청 객체
        ManagerCreateRequest managerRequest = new ManagerCreateRequest(
            "매니저이름",
            "프로필이미지URL",
            "경력 5년",
            "안녕하세요",
            "MALE",
            "자격증이미지URL"
        );

        // 예약 요청 생성
        ReservationRequest reservationRequest = new ReservationRequest(
            newUser.getManager().getId(),
            "department",
            "arrival",
            "2024-05-01 15:25",
            ServiceType.CLINIC_ESCORT,
            Transportation.PUBLIC_TRANSPORTATION,
            10000,
            patientRequest
        );

        ReservationRequest reservationRequest2 = new ReservationRequest(
            newUser.getManager().getId(),
            "department",
            "arrival",
            "2024-05-01 15:25",
            ServiceType.ADMISSION_DISCHARGE_SUPPORT,
            Transportation.PUBLIC_TRANSPORTATION,
            10000,
            patientRequest
        );
        User user3 = userRepository.save(new User("userName3", UserUUID + "USER3", "ROLE_USER"));

        // 예약 생성
        ReservationResponse reservation1 = reservationService.createReservation(reservationRequest2,
            user3);
        ReservationResponse createdReservation = reservationService.createReservation(
            reservationRequest, user);
        assertThat(createdReservation).isNotNull();

        // when
        // 매니저가 연결된 사용자로 조회하기 위해 매니저 정보가 있는 사용자를 다시 조회
        User managerUser = userRepository.findById(newUser.getId()).orElseThrow();
        ReservationResponse reservationResponse = reservationService.
            changeReservationStatus(managerUser, reservation1.reservationId(),
                new ReservationstatusRequest(ReservationStatus.CONFIRMED));

        // then
        assertThat(reservationResponse).isNotNull();
        assertThat(reservationResponse.reservationStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservationResponse).satisfies(
            r -> {
                assertThat(r.reservationDateTime()).isEqualTo(
                    LocalDateTime.of(2024, 5, 1, 15, 25));
                assertThat(r.serviceType()).isEqualTo(ServiceType.ADMISSION_DISCHARGE_SUPPORT);
                assertThat(r.arrivalLocation()).isEqualTo("arrival");
                assertThat(r.departureLocation()).isEqualTo("department");
                assertThat(r.transportation()).isEqualTo(Transportation.PUBLIC_TRANSPORTATION);
                assertThat(r.price()).isEqualTo(10000);
            }
        );

    }

    @Test
    @DisplayName("예약 상태 변경 유효하지 않은 예약 상태로 인한 실패 테스트")
    public void changeReservationStatus_WithInvalidReservationStatus_Test() throws Exception {
        // given
        // 예약 요청 생성
        ReservationRequest reservationRequest = new ReservationRequest(
            newUser.getManager().getId(),
            "department",
            "arrival",
            "2024-05-01 15:25",
            ServiceType.CLINIC_ESCORT,
            Transportation.PUBLIC_TRANSPORTATION,
            10000,
            patientRequest
        );

        ReservationRequest reservationRequest2 = new ReservationRequest(
            newUser.getManager().getId(),
            "department",
            "arrival",
            "2024-05-01 15:25",
            ServiceType.ADMISSION_DISCHARGE_SUPPORT,
            Transportation.PUBLIC_TRANSPORTATION,
            10000,
            patientRequest
        );
        User user3 = userRepository.save(new User("userName3", UserUUID + "USER3", "ROLE_USER"));

        // 예약 생성
        ReservationResponse reservation1 = reservationService.createReservation(reservationRequest2,
            user3);
        ReservationResponse createdReservation = reservationService.createReservation(
            reservationRequest, user);
        assertThat(createdReservation).isNotNull();

        // when & then
        User managerUser = userRepository.findById(newUser.getId()).orElseThrow();
        assertThatThrownBy(() -> reservationService.changeReservationStatus(managerUser,
            reservation1.reservationId(),
            new ReservationstatusRequest(null)))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_RESERVATION_STATUS);
                assertThat(ex.getMessage()).isEqualTo(INVALID_RESERVATION_STATUS.getMessage());
            });
    }

    @Test
    @DisplayName("해당 매니저 앞으로 어떠한 예약도 없는 경우 예약 상태 변경 실패 테스트")
    public void changeReservationStatus_WithEmptyReservation_Test() throws Exception {
        // given
        // 매니저 생성 요청 객체
        User newUserWhoWillBeManager = userRepository.save(
            new User("userName3_MANAGER", UserUUID + UUID.randomUUID().toString(), "ROLE_MANAGER"));
        managerService.createManager(newUserWhoWillBeManager,
            new ManagerCreateRequest("managerName3", "a3", "A3", "a3", "a3", "a3"));

        Random random = new Random();

        // 예약 요청 생성
        ReservationRequest reservationRequest = new ReservationRequest(
            newUserWhoWillBeManager.getManager().getId(),
            "department",
            "arrival",
            "2024-05-01 15:25",
            ServiceType.CLINIC_ESCORT,
            Transportation.PUBLIC_TRANSPORTATION,
            10000,
            patientRequest
        );

        ReservationRequest reservationRequest2 = new ReservationRequest(
            newUserWhoWillBeManager.getManager().getId(),
            "department",
            "arrival",
            "2024-05-01 15:25",
            ServiceType.ADMISSION_DISCHARGE_SUPPORT,
            Transportation.PUBLIC_TRANSPORTATION,
            10000,
            patientRequest
        );
        User user3 = userRepository.save(new User("userName3", UserUUID + "USER3", "ROLE_USER"));

        // 예약 생성
        ReservationResponse reservation1 = reservationService.createReservation(reservationRequest2,
            user3);
        ReservationResponse createdReservation = reservationService.createReservation(
            reservationRequest, user);
        assertThat(createdReservation).isNotNull();

        // when & then
        User managerUser = userRepository.findById(newUser.getId()).orElseThrow();
        assertThatThrownBy(() -> reservationService.changeReservationStatus(managerUser,
            reservation1.reservationId(),
            new ReservationstatusRequest(ReservationStatus.CONFIRMED)))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(RESERVATION_NOT_FOUND);
                assertThat(ex.getMessage()).isEqualTo(RESERVATION_NOT_FOUND.getMessage());
            });
    }

    @Test
    @DisplayName("해당 매니저 앞으로  테스트")
    public void changeReservationStatus_WithWrongReservationId_Test() throws Exception {
        // given
        // 매니저 생성 요청 객체
        Random random = new Random();

        // 예약 요청 생성
        ReservationRequest reservationRequest = new ReservationRequest(
            newUser.getManager().getId(),
            "department",
            "arrival",
            "2024-05-01 15:25",
            ServiceType.CLINIC_ESCORT,
            Transportation.PUBLIC_TRANSPORTATION,
            10000,
            patientRequest
        );

        ReservationRequest reservationRequest2 = new ReservationRequest(
            newUser.getManager().getId(),
            "department",
            "arrival",
            "2024-05-01 15:25",
            ServiceType.ADMISSION_DISCHARGE_SUPPORT,
            Transportation.PUBLIC_TRANSPORTATION,
            10000,
            patientRequest
        );
        User user3 = userRepository.save(new User("userName3", UserUUID + "USER3", "ROLE_USER"));

        // 예약 생성
        ReservationResponse reservation1 = reservationService.createReservation(reservationRequest2,
            user3);
        ReservationResponse createdReservation = reservationService.createReservation(
            reservationRequest, user);
        assertThat(createdReservation).isNotNull();

        // when & then
        User managerUser = userRepository.findById(newUser.getId()).orElseThrow();
        assertThatThrownBy(() -> reservationService.changeReservationStatus(
            newUser,
            reservation1.reservationId() + random.nextLong(),
            new ReservationstatusRequest(ReservationStatus.CONFIRMED)))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(RESERVATION_NOT_BELONG_TO_USER);
                assertThat(ex.getMessage()).isEqualTo(RESERVATION_NOT_BELONG_TO_USER.getMessage());
            });
    }


    @Test
    @DisplayName("매니저용 전체 유저 예약 조회 테스트")
    void getManagerReservation() {
        // given
        // 매니저 생성 요청 객체
        ManagerCreateRequest managerRequest = new ManagerCreateRequest(
            "매니저이름",
            "프로필이미지URL",
            "경력 5년",
            "안녕하세요",
            "MALE",
            "자격증이미지URL"
        );
        User userWhoWillBeManager = new User("managerName", "MANAGERUUID", "ROLE_MANAGER");
        userRepository.save(userWhoWillBeManager);
        ManagerCreateResponse savedManagerResponse = managerService.createManager(
            userWhoWillBeManager, managerRequest);
        ReservationRequest reservationRequest = new ReservationRequest(
            managerRepository.findByUserId(userWhoWillBeManager.getId()).get().getId(),
            "department",
            "arrival",
            "2024-05-01 15:25",
            ServiceType.CLINIC_ESCORT,
            Transportation.PUBLIC_TRANSPORTATION,
            10000,
            patientRequest
        );

        // 예약 생성
        ReservationResponse createdReservation = reservationService.createReservation(
            reservationRequest, user);
        assertThat(createdReservation).isNotNull();

        // when
        User managerUser = userRepository.findById(userWhoWillBeManager.getId()).orElseThrow();
        List<ReservationResponse> managerReservations = reservationService.getManagerReservation(
            managerUser);

        // then
        assertThat(managerReservations).isNotNull();
        assertThat(managerReservations).hasSize(1);
        assertThat(managerReservations.getFirst())
            .satisfies(response -> {
                assertThat(response.departureLocation()).isEqualTo("department");
                assertThat(response.arrivalLocation()).isEqualTo("arrival");
                assertThat(response.serviceType()).isEqualTo(ServiceType.CLINIC_ESCORT);
                assertThat(response.transportation()).isEqualTo(
                    Transportation.PUBLIC_TRANSPORTATION);
                assertThat(response.price()).isEqualTo(10000);
            });
    }

    @Test
    @DisplayName("매니저가 아닌 유저가 매니저용 조회 시 실패 테스트")
    public void findByReservationWrongUserStatusTest() throws Exception {
        // given & when & then
        assertThatThrownBy(() -> reservationService.getManagerReservation(user))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(NOT_MANAGER);
                assertThat(ex.getMessage()).isEqualTo(NOT_MANAGER.getMessage());
            });
    }

    @Test
    @DisplayName("매니저로 등록이 되어 있지 않은 사용자의 경우 매니저용 조회 시 실패 테스트")
    public void findByReservation_WithNotRegisteredUserTest() throws Exception {
        // given
        User notRegisteredManager = new User(null, "notRegisteredManager",
            "UUIDMANAGERNOTRIGSTERED", "ROLE_MANAGER", null, new ArrayList<>());
        userRepository.save(notRegisteredManager);

        // when & then
        assertThatThrownBy(() -> reservationService.getManagerReservation(notRegisteredManager))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(MANAGER_NOT_FOUND);
                assertThat(ex.getMessage()).isEqualTo(MANAGER_NOT_FOUND.getMessage());
            });
    }

    @Test
    @DisplayName("해당 매니저 리스트는 예약이 없는 경우 예약 조회 테스트")
    public void findByReservation_WithNoReservationTest() throws Exception {
        // given & when & then
        assertThatThrownBy(() -> reservationService.getManagerReservation(newUser))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(RESERVATION_NOT_FOUND);
                assertThat(ex.getMessage()).isEqualTo(RESERVATION_NOT_FOUND.getMessage());
            });
    }

    @Test
    @DisplayName("매니저가 예약 취소")
    void managerCancelReservationWithChangeReservationStatusTest() {
        // given
        ManagerCreateRequest managerRequest = new ManagerCreateRequest(
            "매니저이름",
            "프로필이미지URL",
            "경력 5년",
            "안녕하세요",
            "MALE",
            "자격증이미지URL"
        );

        ReservationRequest reservationRequest = new ReservationRequest(
            newUser.getManager().getId(),
            "department",
            "arrival",
            "2024-05-01 15:25",
            ServiceType.CLINIC_ESCORT,
            Transportation.PUBLIC_TRANSPORTATION,
            10000,
            patientRequest
        );
        User user3 = userRepository.save(new User("userName+123", UserUUID + "1234", "ROLE_USER"));
        ReservationResponse firstReservation = reservationService.createReservation(
            reservationRequest, user);
        ReservationResponse secondReservation = reservationService.createReservation(
            reservationRequest, user3);

        // when
        // 매니저 담당 전체 리스트 조회 테스트
        List<ReservationResponse> managerReservation = reservationService.getManagerReservation(
            newUser);
        assertThat(managerReservation).isNotNull();
        assertThat(managerReservation).hasSize(2);
        assertThat(managerReservation).satisfies(
            reservationResponse -> {
                for (ReservationResponse response : reservationResponse) {
                    assertThat(response.price()).isEqualTo(10000);
                    assertThat(response.departureLocation()).isEqualTo("department");
                    assertThat(response.arrivalLocation()).isEqualTo("arrival");
                    assertThat(response.serviceType()).isEqualTo(ServiceType.CLINIC_ESCORT);
                    assertThat(response.transportation()).isEqualTo(
                        Transportation.PUBLIC_TRANSPORTATION);
                    assertThat(response.managerId()).isEqualTo(newUser.getManager().getId());
                }
            }
        );

        // 매니저가 상태 변경 (예약 수락)
        ReservationResponse changedReservationResponse = reservationService.changeReservationStatus(
            newUser, firstReservation.reservationId(),
            new ReservationstatusRequest(ReservationStatus.CONFIRMED));
        assertThat(changedReservationResponse).isNotNull();
        assertThat(changedReservationResponse).satisfies(
            changed -> {
                assertThat(changed.reservationStatus()).isEqualTo(ReservationStatus.CONFIRMED);
                assertThat(changed.managerId()).isEqualTo(newUser.getManager().getId());
                assertThat(changed.serviceType()).isEqualTo(ServiceType.CLINIC_ESCORT);
                assertThat(changed.arrivalLocation()).isEqualTo("arrival");
                assertThat(changed.departureLocation()).isEqualTo("department");
                assertThat(changed.price()).isEqualTo(10000);
            }
        );

        // 매니저가 예약 거절
        ReservationResponse notAcceptedReservation = reservationService.changeReservationStatus(
            newUser,
            secondReservation.reservationId(),
            new ReservationstatusRequest(ReservationStatus.CANCEL)
        );
        assertThat(notAcceptedReservation).isNotNull();
        assertThat(notAcceptedReservation).satisfies(
            changed -> {
                assertThat(changed.reservationStatus()).isEqualTo(ReservationStatus.CANCEL);
                assertThat(changed.managerId()).isEqualTo(newUser.getManager().getId());
                assertThat(changed.serviceType()).isEqualTo(ServiceType.CLINIC_ESCORT);
                assertThat(changed.arrivalLocation()).isEqualTo("arrival");
                assertThat(changed.departureLocation()).isEqualTo("department");
                assertThat(changed.price()).isEqualTo(10000);
            }
        );

        // 매니저 담당 예약 시 취소된 것은 제외
        List<ReservationResponse> managerReservationList = reservationService.getManagerReservation(
            newUser);
        assertThat(managerReservationList).isNotNull();
        assertThat(managerReservationList).hasSize(1);
        assertThat(managerReservationList).satisfies(
            responseList -> {
                for (ReservationResponse reservationResponse : responseList) {
                    assertThat(reservationResponse).isEqualTo(changedReservationResponse);
                }
            }
        );
    }
}