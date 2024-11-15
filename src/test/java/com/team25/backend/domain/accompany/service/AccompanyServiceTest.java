package com.team25.backend.domain.accompany.service;

import static com.team25.backend.global.exception.ErrorCode.INVALID_ACCOMPANY_STATUS;
import static com.team25.backend.global.exception.ErrorCode.REQUIRED_DATE_MISSING;
import static com.team25.backend.global.exception.ErrorCode.REQUIRED_DESCRIPTION_MISSING;
import static com.team25.backend.global.exception.ErrorCode.RESERVATION_NOT_FOUND;
import static com.team25.backend.global.exception.ErrorCode.RESERVATION_WITHOUT_ACCOMPANY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team25.backend.domain.accompany.dto.request.AccompanyRequest;
import com.team25.backend.domain.accompany.dto.response.AccompanyResponse;
import com.team25.backend.domain.accompany.enumdomain.AccompanyStatus;
import com.team25.backend.domain.accompany.repository.AccompanyRepository;
import com.team25.backend.domain.manager.entity.Certificate;
import com.team25.backend.domain.manager.entity.Manager;
import com.team25.backend.domain.manager.repository.ManagerRepository;
import com.team25.backend.domain.patient.entity.Patient;
import com.team25.backend.domain.patient.enumdomain.PatientGender;
import com.team25.backend.domain.patient.repository.PatientRepository;
import com.team25.backend.domain.reservation.entity.Reservation;
import com.team25.backend.domain.reservation.enumdomain.ReservationStatus;
import com.team25.backend.domain.reservation.enumdomain.ServiceType;
import com.team25.backend.domain.reservation.enumdomain.Transportation;
import com.team25.backend.domain.reservation.repository.ReservationRepository;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.domain.user.repository.UserRepository;
import com.team25.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
class AccompanyServiceTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    ManagerRepository managerRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    AccompanyService accompanyService;
    @Autowired
    AccompanyRepository accompanyRepository;

    private User user;
    private Manager manager;
    private Patient patient;
    private final String UserUUID = "uuid";
    private Reservation reservation;
    private Reservation anotherReservation;
    private User userWhoisManager;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("userName", UserUUID, "ROLE_USER"));
        userWhoisManager = userRepository.save(new User("whoisManager", UserUUID, "ROLE_MANAGER"));
        Manager savedManager = managerRepository.save(
            new Manager(null, userWhoisManager, "managerName", "profile", "Career", "comment",
                "region", "gender", true, new ArrayList<Certificate>(), null));
        userWhoisManager.setManager(savedManager);
        patient = patientRepository.save(new Patient(1L, "patient_name", "010-0000-0000",
            PatientGender.MALE, LocalDate.now(), "000-0000-0000", "relation", null));
        reservation = reservationRepository.save(
            new Reservation(null, manager, user, "department", "arrival",
                LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.HOLD, null, null,
                LocalDateTime.now(),
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000, false,
                new ArrayList<>(),
                new ArrayList<>(), patient));
    }

    @AfterEach
    void tearDown() {
        accompanyRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        patientRepository.deleteAllInBatch();
        managerRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("실시간 동행 현황 조회 테스트")
    void getTrackingAccompanies() {
        // given
        AccompanyRequest accompanyRequest = new AccompanyRequest(AccompanyStatus.REGISTER,
            "2024-05-15 15:52", "describe");
        AccompanyResponse accompanyResponse = accompanyService.addTrackingAccompany(
            reservation.getId(), accompanyRequest);
        accompanyService.addTrackingAccompany(reservation.getId(),
            new AccompanyRequest(AccompanyStatus.EXAMINATION, "2024-05-15 16:31", "descirbe2"));

        // when
        List<AccompanyResponse> trackingAccompanies = accompanyService.getTrackingAccompanies(
            reservation.getId());

        // then
        assertThat(trackingAccompanies).hasSize(2);
        assertThat(trackingAccompanies).satisfies(
            list -> {
                for (AccompanyResponse response : list) {
                    if (response.status().equals(AccompanyStatus.REGISTER)) {
                        assertThat(response.statusDescribe()).isEqualTo("describe");
                        assertThat(response.statusDate()).isEqualTo(
                            LocalDateTime.of(2024, 5, 15, 15, 52));
                    } else if (response.status().equals(AccompanyStatus.EXAMINATION)) {
                        assertThat(response.statusDescribe()).isEqualTo("descirbe2");
                        assertThat(response.statusDate()).isEqualTo(
                            LocalDateTime.of(2024, 5, 15, 16, 31));
                    }
                }
            }
        );
    }

    @Test
    @DisplayName("실시간 동행 현황 추가 테스트")
    void addTrackingAccompanyTest() {
        // given
        AccompanyRequest accompanyRequest = new AccompanyRequest(AccompanyStatus.REGISTER,
            "2024-05-15 15:52", "describe");

        // when
        AccompanyResponse accompanyResponse = accompanyService.addTrackingAccompany(
            reservation.getId(), accompanyRequest);

        // then
        assertThat(accompanyResponse).isNotNull();
        assertThat(accompanyResponse).isInstanceOf(AccompanyResponse.class);
        assertThat(accompanyResponse.status()).isEqualTo(AccompanyStatus.REGISTER);
        assertThat(accompanyResponse.statusDate()).isEqualTo(LocalDateTime.of(2024, 5, 15, 15, 52));
        assertThat(accompanyResponse.statusDescribe()).isEqualTo("describe");
    }

    @Test
    @DisplayName("존재하지 않는 예약에 대해 실시간 동행현황 추가 실패 테스트")
    public void addTrackingAccompany_WithNonExistReservation_Test() throws Exception {
        // when & then
        Random random = new Random();
        assertThatThrownBy(
            () -> accompanyService.addTrackingAccompany(reservation.getId() + random.nextLong(),
                new AccompanyRequest(AccompanyStatus.REGISTER, "2024-11-11 11:11", "describe")))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(RESERVATION_NOT_FOUND);
                assertThat(ex.getMessage()).isEqualTo(RESERVATION_NOT_FOUND.getMessage());
            });
    }

    @Test
    @DisplayName("예약 ID로 검색한 예약 리스트가 비었을 경우 accompany 조회 실패 테스트")
    public void findAccompany_WhenReservationNullTest() throws Exception {
        // given
        Long emptyReservationId = 123456789L;

        // when & then
        assertThatThrownBy(() -> accompanyService.getTrackingAccompanies(emptyReservationId))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(RESERVATION_NOT_FOUND);
                assertThat(ex.getMessage()).isEqualTo(RESERVATION_NOT_FOUND.getMessage());
            });
    }

    @Test
    @DisplayName("해당 예약 ID에는 동행 정보가 생성된 적이 없는 경우 조회 실패 테스트")
    public void findAccompany_WhenAccompanyIsEmpty_Test() throws Exception {
        // given
        anotherReservation = reservationRepository.save(
            new Reservation(null, manager, user, "department", "arrival",
                LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.CONFIRMED, null,
                null,
                LocalDateTime.now(),
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 120000, false,
                new ArrayList<>(),
                new ArrayList<>(),
                new Patient(null, "nameNew", "010-0000-000", PatientGender.FEMALE,
                    LocalDate.of(2000, 01, 01), "010-0000-0000", "relation", reservation)
            ));

        AccompanyRequest accompanyRequest = new AccompanyRequest(AccompanyStatus.REGISTER,
            "2024-05-15 15:52", "describe");
        AccompanyResponse accompanyResponse = accompanyService.addTrackingAccompany(
            reservation.getId(), accompanyRequest);
        accompanyService.addTrackingAccompany(reservation.getId(),
            new AccompanyRequest(AccompanyStatus.EXAMINATION, "2024-05-15 16:31", "descirbe2"));

        // when & then
        assertThatThrownBy(
            () -> accompanyService.getTrackingAccompanies(anotherReservation.getId()))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(RESERVATION_WITHOUT_ACCOMPANY);
                assertThat(ex.getMessage()).isEqualTo(RESERVATION_WITHOUT_ACCOMPANY.getMessage());
            });
    }

    @Test
    @DisplayName("실시간 동행 생성 시 유효하지 않은 실시간 현황 상태로 인한 실패 테스트")
    public void addTrackingWihtInvalidAccompanyStatusTest() throws Exception {
        // given
        AccompanyRequest accompanyRequest = new AccompanyRequest(null,
            "2024-05-15 15:52", "describe");

        // when & then
        assertThatThrownBy(() ->
            accompanyService.addTrackingAccompany(reservation.getId(), accompanyRequest))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_ACCOMPANY_STATUS);
                assertThat(ex.getMessage()).isEqualTo(INVALID_ACCOMPANY_STATUS.getMessage());
            });
    }

    @Test
    @DisplayName("실시간 동행 생성 시 실시간 날짜 누락으로 인한 실패 테스트")
    public void addTrackingWihtInvalidDateTest() throws Exception {
        // given
        AccompanyRequest accompanyRequest = new AccompanyRequest(AccompanyStatus.REGISTER,
            "", "describe");

        // when & then
        assertThatThrownBy(() ->
            accompanyService.addTrackingAccompany(reservation.getId(), accompanyRequest))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(REQUIRED_DATE_MISSING);
                assertThat(ex.getMessage()).isEqualTo(REQUIRED_DATE_MISSING.getMessage());
            });
    }

    @Test
    @DisplayName("실시간 동행 생성 시 유효하지 않은 실시간 세부 사항으로 인한 실패 테스트")
    public void addTrackingWihtInvalidStatusDescribeTest() throws Exception {
        // given
        AccompanyRequest accompanyRequest = new AccompanyRequest(AccompanyStatus.EXAMINATION,
            "2024-05-15 15:52", "");

        // when & then
        assertThatThrownBy(() ->
            accompanyService.addTrackingAccompany(reservation.getId(), accompanyRequest))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(REQUIRED_DESCRIPTION_MISSING);
                assertThat(ex.getMessage()).isEqualTo(REQUIRED_DESCRIPTION_MISSING.getMessage());
            });
    }

}