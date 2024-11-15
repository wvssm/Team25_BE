package com.team25.backend.domain.accompany.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team25.backend.domain.accompany.entity.Accompany;
import com.team25.backend.domain.accompany.enumdomain.AccompanyStatus;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@AutoConfigureTestDatabase(replace = Replace.ANY)
@ActiveProfiles("test")
@DataJpaTest
class AccompanyRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(AccompanyRepositoryTest.class);
    @Autowired
    UserRepository userRepository;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    ManagerRepository managerRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    AccompanyRepository accompanyRepository;

    private User user;
    private Manager manager;
    private Patient patient;
    private final String UserUUID = "uuid";
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("userName", UserUUID, "ROLE_USER"));
        manager = managerRepository.save(
            new Manager(1L, user, "managerName", "profileImage", "career", "comment",
                "workingRegion", "gender", false, new ArrayList<>(), null));
        patient = patientRepository.save(new Patient(1L, "patient_name", "010-0000-0000",
            PatientGender.MALE, LocalDate.now(), "000-0000-0000", "relation", null));
        reservation = reservationRepository.save(
            new Reservation(null, manager, user, "department", "arrival",
                LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.HOLD, null, null,
                LocalDateTime.now(),
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000, false, null,
                null, patient));
    }

    @Test
    @DisplayName("동행 정보 저장 테스트")
    public void saveAccompanytest() throws Exception {
        // given
        Accompany accompany = new Accompany(null, reservation, AccompanyStatus.REGISTER,
            LocalDateTime.of(2024, 11, 10, 15, 42), "detail");

        // when
        Accompany savedAccomapny = accompanyRepository.save(accompany);

        // then
        log.info("=".repeat(80));
        log.info("테스트 케이스: 동행 정보 저장 테스트");
        log.info("검증 항목:");
        log.info("1. 저장된 동행 상태: {}", savedAccomapny.getAccompanyStatus());
        log.info("2. 저장된 상세 설명: {}", savedAccomapny.getDetail());
        log.info("3. 저장된 예약 정보 ID: {}", savedAccomapny.getReservation().getId());
        log.info("4. 저장된 동행 시간: {}", savedAccomapny.getTime());
        log.info("테스트 결과: 성공 ✅");
        log.info("=".repeat(80));
        assertThat(savedAccomapny).isNotNull();
        assertThat(savedAccomapny).satisfies(
            checkedAccompany -> {
                assertThat(checkedAccompany.getAccompanyStatus()).isEqualTo(
                    AccompanyStatus.REGISTER);
                assertThat(checkedAccompany.getDetail()).isEqualTo("detail");
                assertThat(checkedAccompany.getReservation()).isEqualTo(reservation);
                assertThat(checkedAccompany.getTime()).isEqualTo(
                    LocalDateTime.of(2024, 11, 10, 15, 42));
            }
        );
    }

    @Test
    @DisplayName("동행 정보 조회 테스트")
    public void retrieveAccomapnyByReservationIdTest() throws Exception {
        // given
        Accompany accompany = new Accompany(null, reservation, AccompanyStatus.VISIT_PHARMACY,
            LocalDateTime.of(2024, 11, 10, 15, 42), "detail");
        Accompany accompany1 = new Accompany(null, reservation, AccompanyStatus.RETURN_HOME,
            LocalDateTime.of(2024, 11, 10, 15, 55), "detail");
        accompanyRepository.save(accompany);
        accompanyRepository.save(accompany1);

        // when
        List<Accompany> savedAccompanyList = accompanyRepository.findByReservation_id(
            reservation.getId());

        // then
        log.info("=".repeat(80));
        log.info("테스트 케이스: 동행 정보 조회 테스트");
        log.info("입력 데이터:");
        log.info("- 예약 ID: {}", reservation.getId());
        log.info("조회 결과:");
        log.info("- 조회된 동행 정보 개수: {}", savedAccompanyList.size());

        for (int i = 0; i < savedAccompanyList.size(); i++) {
            Accompany acc = savedAccompanyList.get(i);
            log.info("동행 정보 #{}", i + 1);
            log.info("- 동행 상태: {}", acc.getAccompanyStatus());
            log.info("- 상세 설명: {}", acc.getDetail());
            log.info("- 동행 시간: {}", acc.getTime());
        }

        log.info("검증 결과:");
        log.info("- 전체 동행 정보 개수 일치: {}", savedAccompanyList.size() == 2 ? "성공 ✅" : "실패 ❌");
        log.info("- 모든 동행 정보의 예약 ID 일치 여부: 성공 ✅");
        log.info("- 동행 상태 및 시간 정보 일치 여부: 성공 ✅");
        log.info("=".repeat(80));
        assertThat(savedAccompanyList).isNotNull();
        assertThat(savedAccompanyList.size()).isEqualTo(2);
        assertThat(savedAccompanyList).satisfies(
            list -> {
                for (Accompany acc : list) {
                    assertThat(acc.getReservation()).isEqualTo(reservation);
                    assertThat(acc.getDetail()).isEqualTo("detail");
                    if (acc.getAccompanyStatus().equals(AccompanyStatus.VISIT_PHARMACY)) {
                        assertThat(acc.getTime()).isEqualTo(LocalDateTime.of(2024, 11, 10, 15, 42));
                    } else if (acc.getAccompanyStatus().equals(AccompanyStatus.RETURN_HOME)) {
                        assertThat(acc.getTime()).isEqualTo(LocalDateTime.of(2024, 11, 10, 15, 55));
                    }
                }
            }
        );
    }

    @Test
    @DisplayName("잘못된 예약 ID로 동행 정보 조회 테스트")
    public void findByWrongReservationIDTest() throws Exception {
        // given
        Long wrongId = 1234566789L;

        // when
        List<Accompany> accompanyList = accompanyRepository.findByReservation_id(wrongId);

        // then
        assertThat(accompanyList).isNotNull();
        assertThat(accompanyList.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("실시간 동행 현황 빌더 테스트")
    public void accompanyBuilderTest() throws Exception {
        // given
        Accompany builderAccompany = Accompany.builder().accompanyStatus(AccompanyStatus.VISIT_PHARMACY)
            .detail("detail")
            .time(LocalDateTime.of(2024, 11, 10, 15, 42))
            .build();

        // when
        Accompany savedAccompany = accompanyRepository.save(builderAccompany);

        // then
        assertThat(savedAccompany).isNotNull();
    }


}