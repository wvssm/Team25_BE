package com.team25.backend.domain.report.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team25.backend.domain.manager.entity.Manager;
import com.team25.backend.domain.manager.repository.CertificateRepository;
import com.team25.backend.domain.manager.repository.ManagerRepository;
import com.team25.backend.domain.manager.repository.WorkingHourRepository;
import com.team25.backend.domain.patient.entity.Patient;
import com.team25.backend.domain.patient.enumdomain.PatientGender;
import com.team25.backend.domain.patient.repository.PatientRepository;
import com.team25.backend.domain.report.entity.Report;
import com.team25.backend.domain.report.enumdomain.MedicineTime;
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
import java.util.NoSuchElementException;
import java.util.Random;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@AutoConfigureTestDatabase(replace = Replace.ANY)
@ActiveProfiles("test")
@DataJpaTest
class ReportRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    ManagerRepository managerRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    private TestEntityManager entityManager;

    private User user;
    private Manager manager;
    private Patient patient;
    private final String UserUUID = "uuid";
    private Reservation reservation;
    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private WorkingHourRepository workingHourRepository;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("userName", UserUUID, "ROLE_USER"));
        entityManager.flush();

        manager = managerRepository.save(
            new Manager(1L, user, "managerName", "profileImage", "career", "comment",
                "workingRegion", "gender", false, new ArrayList<>(), null));
        entityManager.flush();

        patient = patientRepository.save(new Patient(1L, "patient_name", "010-0000-0000",
            PatientGender.MALE, LocalDate.now(), "000-0000-0000", "relation", null));
        entityManager.flush();

        reservation = reservationRepository.save(
            new Reservation(null, manager, user, "department", "arrival",
                LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.HOLD, null, null,
                LocalDateTime.now(),
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000, false, null,
                null, patient));
        entityManager.flush();
        entityManager.clear();
    }

    @AfterEach
    void tearDown() {
        try {
            reportRepository.deleteAllInBatch();
            reservationRepository.deleteAllInBatch();
            certificateRepository.deleteAllInBatch();
            workingHourRepository.deleteAllInBatch();
            patientRepository.deleteAllInBatch();
            managerRepository.deleteAllInBatch();
            userRepository.deleteAllInBatch();
        } catch (Exception e) {
        }
    }

    @Test
    @DisplayName("리포트 저장 테스트")
    void saveReportTest() {
        // given
        Report report = new Report(null, reservation, "doctorsummary", 3, MedicineTime.AFTER_MEAL,
            "아침점심저녁");

        // when
        reportRepository.save(report);

        // then
        assertThat(reportRepository.findAll()).hasSize(1);
        assertThat(reportRepository.findAll().getFirst()).isEqualTo(report);
        assertThat(reportRepository.findAll().getLast()).isEqualTo(report);
        assertThat(reportRepository.findAll().getFirst()).satisfies(
            checkedReport -> {
                assertThat(checkedReport.getMedicineTime()).isEqualTo(MedicineTime.AFTER_MEAL);
                assertThat(checkedReport.getFrequency()).isEqualTo(3);
                assertThat(checkedReport.getDoctorSummary()).isEqualTo("doctorsummary");
                assertThat(checkedReport.getReservation()).isEqualTo(reservation);
                assertThat(checkedReport.getTimeOfDay()).isEqualTo("아침점심저녁");
            }
        );
    }


    @Test
    @DisplayName("리포트 필수 필드 누락 테스트 - medicineTime null")
    void saveReportWithNullMedicineTimeTest() {
        // given
        Report report = Report.builder()
            .reservation(reservation)
            .doctorSummary("doctorsummary")
            .frequency(3)
            .medicineTime(null)  // null medicineTime
            .timeOfDay("아침점심저녁")
            .build();

        // when & then
        assertThatThrownBy(() -> {
            reportRepository.save(report);
            entityManager.flush();
        }).hasCauseInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("리포트 조회 테스트")
    void findReportTest() throws Exception {
        // given
        Report report = new Report(null, reservation, "doctorsummary", 3, MedicineTime.AFTER_MEAL,
            "아침점심저녁");
        Report savedReport = reportRepository.save(report);

        // when
        Report foundByIdReport = reportRepository.findById(savedReport.getId()).orElseThrow();

        // then
        assertThat(foundByIdReport).isNotNull();
        assertThat(foundByIdReport).isInstanceOf(Report.class);
        assertThat(foundByIdReport).satisfies(
            checkedReport -> {
                assertThat(checkedReport.getMedicineTime()).isEqualTo(MedicineTime.AFTER_MEAL);
                assertThat(checkedReport.getFrequency()).isEqualTo(3);
                assertThat(checkedReport.getDoctorSummary()).isEqualTo("doctorsummary");
                assertThat(checkedReport.getReservation()).isEqualTo(reservation);
                assertThat(checkedReport.getTimeOfDay()).isEqualTo("아침점심저녁");
            }
        );
    }

    @Test
    @DisplayName("리포트 조회 실패 테스트(잘못됫 리포트 ID)")
    void findReportByWrongIDTest() {
        // given
        Report report = new Report(null, reservation, "doctorsummary", 3, MedicineTime.AFTER_MEAL,
            "아침점심저녁");
        Report savedReport = reportRepository.save(report);
        Random random = new Random();

        // when & then
        assertThatThrownBy(() ->
            reportRepository.findById(savedReport.getId() + random.nextLong()).get())
            .isInstanceOf(NoSuchElementException.class);
    }


    @Test
    @DisplayName("리포트 필수 필드 누락 테스트 - meal time null")
    void saveReportWithNullMealTimeTest() {
        // given
        Report report = Report.builder()
            .reservation(reservation)
            .doctorSummary("doctorsummary")
            .frequency(3)
            .medicineTime(null)
            .timeOfDay("아침점심저녁")
            .build();

        // when & then
        assertThatThrownBy(() -> {
            reportRepository.save(report);
            entityManager.flush();
        }).hasCauseInstanceOf(ConstraintViolationException.class)
            .hasMessageContaining("NULL not allowed for column \"MEAL_TIME\"");
    }

    @Test
    @DisplayName("리포트 필수 필드 누락 테스트 - doctorSummary null")
    void saveReportWithNullDoctorSummaryTest() {
        // given
        Report report = Report.builder()
            .reservation(reservation)
            .doctorSummary(null)
            .frequency(3)
            .medicineTime(MedicineTime.AFTER_MEAL)
            .timeOfDay("아침점심저녁")
            .build();

        // when & then
        assertThatThrownBy(() -> {
            reportRepository.save(report);
            entityManager.flush();
        }).hasCauseInstanceOf(ConstraintViolationException.class)
            .hasMessageContaining("NULL not allowed for column \"DOCTOR_SUMMARY\"");
    }

    @Test
    @DisplayName("리포트 필수 필드 누락 테스트 - timeOfDay null")
    void saveReportWithNullTimeOfDayTest() {
        // given
        Report report = Report.builder()
            .reservation(reservation)
            .doctorSummary("doctorsummary")
            .frequency(3)
            .medicineTime(MedicineTime.AFTER_MEAL)
            .timeOfDay(null)
            .build();

        // when & then
        assertThatThrownBy(() -> {
            reportRepository.save(report);
            entityManager.flush();
        }).hasCauseInstanceOf(ConstraintViolationException.class)
            .hasMessageContaining("NULL not allowed for column \"TIME_OF_DAY\"");
    }

    @Test
    @DisplayName("리포트 필수 필드 누락 테스트 - timeOfDay null")
    void saveReportWithNullReservationTest() {
        // given
        Report report = Report.builder()
            .reservation(null)
            .doctorSummary("doctorsummary")
            .frequency(3)
            .medicineTime(MedicineTime.AFTER_MEAL)
            .timeOfDay("아침점심저녁")
            .build();

        // when & then
        assertThatThrownBy(() -> {
            reportRepository.save(report);
            entityManager.flush();
        }).hasCauseInstanceOf(ConstraintViolationException.class)
            .hasMessageContaining("NULL not allowed for column \"RESERVATION_ID\"");
    }

    @Test
    @DisplayName("Report Builder 저장 테스트")
    public void saveReportByBuilderTest() throws Exception{
        // given
        Report reportByBuilder = Report.builder()
            .reservation(reservation)
            .doctorSummary("doctorsummary")
            .frequency(3)
            .medicineTime(MedicineTime.AFTER_MEAL)
            .timeOfDay("아침")
            .build();

        // when
        Report savedReport = reportRepository.save(reportByBuilder);

        // then
        assertThat(savedReport).isNotNull();
    }

}