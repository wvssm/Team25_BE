package com.team25.backend.domain.report.service;

import static com.team25.backend.global.exception.ErrorCode.INVALID_FREQUENCY;
import static com.team25.backend.global.exception.ErrorCode.INVALID_MEDICINE_TIME;
import static com.team25.backend.global.exception.ErrorCode.REQUIRED_DOCTOR_SUMMARY_MISSING;
import static com.team25.backend.global.exception.ErrorCode.REQUIRED_TIME_OF_DAYS_MISSING;
import static com.team25.backend.global.exception.ErrorCode.RESERVATION_NOT_FOUND;
import static com.team25.backend.global.exception.ErrorCode.RESERVATION_WITHOUT_REPORT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team25.backend.domain.manager.entity.Manager;
import com.team25.backend.domain.manager.repository.ManagerRepository;
import com.team25.backend.domain.patient.dto.request.PatientRequest;
import com.team25.backend.domain.patient.entity.Patient;
import com.team25.backend.domain.patient.enumdomain.PatientGender;
import com.team25.backend.domain.patient.repository.PatientRepository;
import com.team25.backend.domain.report.dto.request.ReportRequest;
import com.team25.backend.domain.report.dto.response.ReportResponse;
import com.team25.backend.domain.report.entity.Report;
import com.team25.backend.domain.report.enumdomain.MedicineTime;
import com.team25.backend.domain.report.repository.ReportRepository;
import com.team25.backend.domain.reservation.dto.request.ReservationRequest;
import com.team25.backend.domain.reservation.dto.response.ReservationResponse;
import com.team25.backend.domain.reservation.entity.Reservation;
import com.team25.backend.domain.reservation.enumdomain.ReservationStatus;
import com.team25.backend.domain.reservation.enumdomain.ServiceType;
import com.team25.backend.domain.reservation.enumdomain.Transportation;
import com.team25.backend.domain.reservation.repository.ReservationRepository;
import com.team25.backend.domain.reservation.service.ReservationService;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.domain.user.repository.UserRepository;
import com.team25.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@SpringBootTest
class ReportServiceTest {

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
    private ReportService reportService;
    @Autowired
    private ReservationService reservationService;

    private User user;
    private Manager manager;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        String userUUID = "uuid";
        user = userRepository.save(new User("userName", userUUID, "ROLE_USER"));
        User userWhoisManager = userRepository.save(
            new User("whoisManager", userUUID, "ROLE_MANAGER"));
        Manager savedManager = managerRepository.save(
            new Manager(null, userWhoisManager, "managerName", "profile", "Career", "comment",
                "region", "gender", true, new ArrayList<>(), null));
        userWhoisManager.setManager(savedManager);
        Patient patient = patientRepository.save(new Patient(1L, "patient_name", "010-0000-0000",
            PatientGender.MALE, LocalDate.now(), "000-0000-0000", "relation", null));
        reservation = reservationRepository.save(
            new Reservation(null, manager, user, "department", "arrival",
                LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.HOLD, null, null,
                LocalDateTime.now(),
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000, false,
                new ArrayList<>(),
                null, patient));
    }

    @AfterEach
    void tearDown() {
        reportRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        patientRepository.deleteAllInBatch();
        managerRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("리포트 생성 테스트")
    void createReport() {
        // given
        PatientRequest patientRequest = new PatientRequest("name", "010-0000-0000",
            PatientGender.MALE, "relation", "1000-02-15", "010-0000-0000");
        ReservationResponse reservation1 = reservationService.createReservation(
            new ReservationRequest(managerRepository.findAll().getFirst().getId(), "d", "a",
                "2021-01-15 12:42", ServiceType.CLINIC_ESCORT,
                Transportation.PUBLIC_TRANSPORTATION, 10000, patientRequest), user);
        ReportRequest reportRequest = new ReportRequest("doctorsummary", 3, MedicineTime.AFTER_MEAL,
            "timeOFDays");
        Reservation reservation2 = reservationRepository.findById(reservation1.reservationId())
            .orElseThrow();

        // when
        ReportResponse createdReport = reportService.createReport(reservation2.getId(),
            reportRequest);

        // then
        assertThat(createdReport).isNotNull();
        assertThat(createdReport).satisfies(
            reportResponse -> {
                assertThat(reportResponse).isNotNull();
                assertThat(reportResponse.doctorSummary()).isEqualTo("doctorsummary");
                assertThat(reportResponse.frequency()).isEqualTo(3);
                assertThat(reportResponse.medicineTime()).isEqualTo(
                    MedicineTime.AFTER_MEAL.toString());
                assertThat(reportResponse.timeOfDays()).isEqualTo("timeOFDays");
            }
        );
    }

    @Test
    @DisplayName("리포트 의사 소견 누락 생성 테스트")
    public void createReportWithNull() {
        PatientRequest patientRequest = new PatientRequest("name", "010-0000-0000",
            PatientGender.MALE, "relation", "1000-02-15", "010-0000-0000");
        ReservationResponse reservation1 = reservationService.createReservation(
            new ReservationRequest(
                managerRepository.findAll().getFirst().getId(), "d", "a", "2021-01-15 12:42",
                ServiceType.CLINIC_ESCORT,
                Transportation.PUBLIC_TRANSPORTATION, 10000, patientRequest), user);
        ReportRequest reportRequest = new ReportRequest("", 3, MedicineTime.AFTER_MEAL,
            "timeOFDays");
        Reservation reservation2 = reservationRepository.findById(reservation1.reservationId())
            .orElseThrow();

        // when
        assertThatThrownBy(() -> reportService.createReport(reservation2.getId(), reportRequest))
            .isInstanceOf(CustomException.class)
            .satisfies(
                exception -> {
                    CustomException ex = (CustomException) exception;
                    assertThat(ex.getErrorCode()).isEqualTo(REQUIRED_DOCTOR_SUMMARY_MISSING);
                    assertThat(ex.getMessage()).isEqualTo(
                        REQUIRED_DOCTOR_SUMMARY_MISSING.getMessage());
                }
            );
    }


    @Test
    @DisplayName("리포트 조회 테스트")
    void getReport() {
        // given
        Report report = new Report(null, reservation, "doctorsummary", 3, MedicineTime.AFTER_MEAL,
            "timeOfDay");
        reportRepository.save(report);

        // when
        List<ReportResponse> reportList = reportService.getReport(reservation.getId());

        // then
        assertThat(reportList).isNotNull();
        assertThat(reportList.size()).isEqualTo(1);
        assertThat(reportList).satisfies(
            reportResponseList -> {
                for (ReportResponse reservationResponse : reportResponseList) {
                    assertThat(reservationResponse).isNotNull();
                    assertThat(reservationResponse.doctorSummary()).isEqualTo("doctorsummary");
                    assertThat(reservationResponse.frequency()).isEqualTo(3);
                    assertThat(reservationResponse.medicineTime()).isEqualTo(
                        MedicineTime.AFTER_MEAL.toString());
                    assertThat(reservationResponse.timeOfDays()).isEqualTo("timeOfDay");
                }
            }
        );
    }

    @Test
    @DisplayName("리포트가 없는 예약의 경우 리포트 없음 예외 테스트")
    public void retrieveReportyByUnreportedReservation() {
        // given & when & then
        assertThatThrownBy(() -> reportService.getReport(reservation.getId()))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(RESERVATION_WITHOUT_REPORT);
                assertThat(ex.getMessage()).isEqualTo(RESERVATION_WITHOUT_REPORT.getMessage());
            });
    }

    @Test
    @DisplayName("미생성 예약에 대한 리포트 생성 시도")
    public void createReportWithEmptyReservation() {
        // given
        Long nonExistReservationId = 987654321L;
        ReportRequest reportRequest = new ReportRequest(
            "summary"
            , 3,
            MedicineTime.AFTER_MEAL,
            "아침저녁"
        );

        // when & then
        assertThatThrownBy(() -> reportService.createReport(nonExistReservationId, reportRequest))
            .isInstanceOf(CustomException.class)
            .satisfies(
                exception -> {
                    CustomException ex = (CustomException) exception;
                    assertThat(ex.getErrorCode()).isEqualTo(RESERVATION_NOT_FOUND);
                    assertThat(ex.getMessage()).isEqualTo(RESERVATION_NOT_FOUND.getMessage());
                }
            );
    }

    @Test
    @DisplayName("MEDICINTIME 검증 테스트")
    public void createReportWithoutMedicineTime() {
        // given
        ReportRequest reportRequest = new ReportRequest(
            "summary",
            3,
            null,
            "아침저녁"
        );

        // when & then
        assertThatThrownBy(() -> reportService.createReport(reservation.getId(), reportRequest))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                    CustomException ex = (CustomException) exception;
                    assertThat(ex.getErrorCode()).isEqualTo(INVALID_MEDICINE_TIME);
                    assertThat(ex.getMessage()).isEqualTo(INVALID_MEDICINE_TIME.getMessage());
                }
            );
    }

    @Test
    @DisplayName("TIMDOFDAY 누락 검증 테스트")
    public void createReportWithoutTimeOfDay() {
        // given
        ReportRequest reportRequest = new ReportRequest("summary", 3, MedicineTime.AFTER_MEAL,
            null);

        // when & then
        assertThatThrownBy(() -> reportService.createReport(reservation.getId(), reportRequest))
            .isInstanceOf(CustomException.class)
            .satisfies(
                exception -> {
                    CustomException ex = (CustomException) exception;
                    assertThat(ex.getErrorCode()).isEqualTo(REQUIRED_TIME_OF_DAYS_MISSING);
                    assertThat(ex.getMessage()).isEqualTo(
                        REQUIRED_TIME_OF_DAYS_MISSING.getMessage());
                }
            );
    }

    @Test
    @DisplayName("FREQUENCY INVALID 테스트")
    public void invalidFrequencyTest() {
        // given
        int frequency = -216;

        // when
        assertThatThrownBy(() -> ReportService.validateReportRequest(
            new ReportRequest("summary", frequency, MedicineTime.IN_MEAL, "아침")))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException ex = (CustomException) exception;
                assertThat(ex.getErrorCode()).isEqualTo(INVALID_FREQUENCY);
                assertThat(ex.getMessage()).isEqualTo(INVALID_FREQUENCY.getMessage());
            });

        // then
    }

}