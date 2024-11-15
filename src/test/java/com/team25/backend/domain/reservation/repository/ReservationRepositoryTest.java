package com.team25.backend.domain.reservation.repository;

import static com.team25.backend.global.exception.ErrorCode.RESERVATION_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

import com.team25.backend.domain.manager.entity.Manager;
import com.team25.backend.domain.manager.repository.ManagerRepository;
import com.team25.backend.domain.patient.entity.Patient;
import com.team25.backend.domain.patient.enumdomain.PatientGender;
import com.team25.backend.domain.patient.repository.PatientRepository;
import com.team25.backend.domain.reservation.entity.Reservation;
import com.team25.backend.domain.reservation.enumdomain.CancelReason;
import com.team25.backend.domain.reservation.enumdomain.ReservationStatus;
import com.team25.backend.domain.reservation.enumdomain.ServiceType;
import com.team25.backend.domain.reservation.enumdomain.Transportation;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.domain.user.repository.UserRepository;
import com.team25.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@Transactional
@AutoConfigureTestDatabase(replace = Replace.ANY)
@ActiveProfiles("test")
@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    ManagerRepository managerRepository;
    @Autowired
    ReservationRepository reservationRepository;


    private User user;
    private Manager manager;
    private Patient patient;
    private final String UserUUID = "uuid";


    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("userName", UserUUID, "ROLE_USER"));
        manager = managerRepository.save(
            new Manager(null, user, "managerName", "profileImage", "career", "comment",
                "workingRegion", "gender", false, new ArrayList<>(), null));
        patient = patientRepository.save(new Patient(1L, "patient_name", "010-0000-0000",
            PatientGender.MALE, LocalDate.now(), "000-0000-0000", "relation", null));
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAllInBatch();
        patientRepository.deleteAllInBatch();
        managerRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    // 성공 테스트

    @Test
    @DisplayName("예약 저장 테스트")
    void saveReservationTest() {
        // given
        Reservation reservation = new Reservation(null, manager, user, "department", "arrival",
            LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.HOLD, null, null,
            LocalDateTime.now(),
            ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000, false, null,
            null, patient);

        // when
        Reservation save = reservationRepository.save(reservation);

        // then
        assertThat(reservationRepository.findAll()).hasSize(1);
        assertThat(reservationRepository.findAll().getFirst()).isEqualTo(reservation);
    }


    @Test
    @DisplayName("예약 ID로 검색 테스트")
    public void findByReservationIdTest() throws Exception {
        // given
        Reservation reservation = new Reservation(null, manager, user, "department", "arrival",
            LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.HOLD, null, null,
            LocalDateTime.now(),
            ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000, false, null,
            null, patient);

        // when
        Reservation save = reservationRepository.save(reservation);

        // then
        assertThat(reservationRepository.findById(reservation.getId()).orElseThrow(
            () -> new CustomException(RESERVATION_NOT_FOUND))).isEqualTo(
            save);
    }

    @Test
    @DisplayName("사용자 UUID로 예약 리스트 조회")
    public void findByUserUuidTest() throws Exception {
        // given
        Reservation reservation = new Reservation(null, manager, user, "department", "arrival",
            LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.HOLD, null, null,
            LocalDateTime.now(),
            ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000, false, null,
            null, patient);
        ArrayList<Reservation> reservations = new ArrayList<>();
        reservations.add(reservation);

        // when
        Reservation save = reservationRepository.save(reservation);

        // then
        assertThat(reservationRepository.findByUser_Uuid(UserUUID)).isEqualTo(reservations);
    }

    @Test
    @DisplayName("예약 정보 수정 성공 테스트")
    void updateReservationTest() {
        // given
        Reservation reservation = new Reservation(null, manager, user, "department", "arrival",
            LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.HOLD, null, null,
            LocalDateTime.now(),
            ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000, false, null,
            null, patient);
        ArrayList<Reservation> reservations = new ArrayList<>();
        reservations.add(reservation);

        // when
        Reservation save = reservationRepository.save(reservation);

        // then
        assertThat(reservationRepository.findByUser_Uuid(UserUUID)).isEqualTo(reservations);
    }

    // 실패 케이스 - Null 값 테스트
    @Test
    @DisplayName("Null UUID로 예약 조회시 실패 테스트")
    void findByUser_UuidTest_NullUuid() {
        // given
        Reservation reservation = new Reservation(null, manager, user, "department", "arrival",
            LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.HOLD, null, null,
            LocalDateTime.now(),
            ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000, false, null,
            null, patient);
        ArrayList<Reservation> reservations = new ArrayList<>();
        reservations.add(reservation);

        // when
        List<Reservation> result = reservationRepository.findByUser_Uuid(null);

        // then
        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("환자 정보 누락으로 예약 저장 실패 테스트")
    void saveTest_NullPatient() {
        // given
        Reservation reservation = new Reservation(null, manager, user, "department", "arrival",
            LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.HOLD, null, null,
            LocalDateTime.now(),
            ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000, false, null,
            null, patient);
        reservation.setPatient(null);

        // when
        Reservation save = reservationRepository.save(reservation);

        // then
        assertThat(reservationRepository.findAll()).hasSize(1);
        assertThat(reservationRepository.findAll().get(0)).isEqualTo(reservation);
    }


    // 실패 케이스 - 조회 실패
    @Test
    @DisplayName("존재하지 않는 예약 ID로 조회시 실패 테스트")
    void findByIdTest_NotFound() {
        Reservation nonExistentReservation = new Reservation(null, manager, user, "department",
            "arrival",
            LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.HOLD, null, null,
            LocalDateTime.now(), ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION,
            10000, false, null, null, patient);

        Long nonExistentId = 999L;  // 존재하지 않는 ID

        // when
        Optional<Reservation> result = reservationRepository.findById(nonExistentId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("예약이 없는 유저의 UUID로 조회시 빈 리스트 반환 테스트")
    void findByUser_UuidTest_EmptyResult() {
        // given
        User anotherUser = userRepository.save(new User("userName", UserUUID + "ABC", "ROLE_USER"));
        Reservation reservation = new Reservation(null, manager, user, "department", "arrival",
            LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.HOLD, null, null,
            LocalDateTime.now(),
            ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000, false, null,
            null, patient);

        // when
        reservationRepository.save(reservation);

        // then
        assertThat(reservationRepository.findByUser_Uuid(anotherUser.getUuid())).isEmpty();
    }

    @Test
    @DisplayName("매니저의 예약 취소 테스트")
    public void cancelReservationTest() {
        // given
        // 예약 생성
        Reservation reservation = new Reservation(null, manager, user, "department", "arrival",
            LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.HOLD, null, null,
            LocalDateTime.now(),
            ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000, false, null,
            null, patient);
        Reservation savedReservation = reservationRepository.save(reservation);

        // when
        List<Reservation> retrievedReservationByManger = reservationRepository.findByManager_Id(
            manager.getId());
        Reservation first = retrievedReservationByManger.getFirst();
        first.setReservationStatus(ReservationStatus.CANCEL);
        first.setCancelReason(CancelReason.PATIENT_CANCEL);
        first.setCancelDetail("Detail");
        reservationRepository.save(first);

        // then
        assertThat(reservationRepository).isNotNull();
        assertThat(reservationRepository.findAll()).hasSize(1);
        assertThat(reservationRepository.findById(first.getId()).get()).isEqualTo(first);
        assertThat(reservationRepository.findById(first.getId()).orElseThrow()).satisfies(
            reservation1 -> {
                assertThat(reservation1.getReservationStatus()).isEqualTo(ReservationStatus.CANCEL);
                assertThat(reservation1.getCancelReason()).isEqualTo(CancelReason.PATIENT_CANCEL);
                assertThat(reservation1.getCancelDetail()).isEqualTo("Detail");
            }
        );
    }

    @Test
    @DisplayName("빌더로 예약 생성 후 저장 테스트")
    public void reservationBuilderTest() throws Exception {
        // given
        Reservation buildedReservation = Reservation.builder().reservationStatus(ReservationStatus.CONFIRMED)
            .reservationDateTime(LocalDateTime.now())
            .reports(null)
            .creationDate(LocalDateTime.now())
            .createdTime(LocalDateTime.now())
            .user(user)
            .manager(manager)
            .accompany(null)
            .arrivalLocation("arrival")
            .departureLocation("department")
            .price(20000)
            .patient(patient)
            .build();

        // when
        Reservation savedBuildedReservation = reservationRepository.save(buildedReservation);

        // then
        assertThat(savedBuildedReservation).isNotNull();
        assertThat(savedBuildedReservation.getReservationStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(savedBuildedReservation.getReports()).isNull();
        assertThat(savedBuildedReservation.getUser()).isEqualTo(user);
        assertThat(savedBuildedReservation.getManager()).isEqualTo(manager);
        assertThat(savedBuildedReservation.getAccompany()).isNull();
        assertThat(buildedReservation.getArrivalLocation()).isEqualTo("arrival");
        assertThat(buildedReservation.getDepartureLocation()).isEqualTo("department");
        assertThat(buildedReservation.getPrice()).isEqualTo(20000);
        assertThat(buildedReservation.getReports()).isNull();
    }



}