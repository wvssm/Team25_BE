package com.team25.backend.domain.payment.repository;

import com.team25.backend.domain.manager.entity.Manager;
import com.team25.backend.domain.manager.repository.ManagerRepository;
import com.team25.backend.domain.patient.entity.Patient;
import com.team25.backend.domain.patient.enumdomain.PatientGender;
import com.team25.backend.domain.patient.repository.PatientRepository;
import com.team25.backend.domain.payment.entity.Payment;
import com.team25.backend.domain.reservation.entity.Reservation;
import com.team25.backend.domain.reservation.enumdomain.ReservationStatus;
import com.team25.backend.domain.reservation.enumdomain.ServiceType;
import com.team25.backend.domain.reservation.enumdomain.Transportation;
import com.team25.backend.domain.reservation.repository.ReservationRepository;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@Transactional
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private PatientRepository patientRepository;

    private User user;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        // User 생성 및 저장
        user = userRepository.save(new User("testUsername", "test-uuid", "ROLE_USER"));

        // Manager 생성 및 저장
        Manager manager = managerRepository.save(new Manager(null, user, "managerName", "profileImage", "career", "comment",
                "workingRegion", "gender", false, new ArrayList<>(), null));

        // Patient 생성 및 저장
        Patient patient = patientRepository.save(new Patient(1L, "patient_name", "010-0000-0000",
                PatientGender.MALE, LocalDate.now(), "000-0000-0000", "relation", null));

        // Reservation 생성 및 저장
        reservation = reservationRepository.save(new Reservation(null, manager, user, "department", "arrival",
                LocalDateTime.now(), LocalDateTime.now(), false, ReservationStatus.HOLD, null, null,
                LocalDateTime.now(),
                ServiceType.CLINIC_ESCORT, Transportation.PUBLIC_TRANSPORTATION, 10000, false, null,
                null, patient));
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        patientRepository.deleteAllInBatch();
        managerRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Payment 저장 테스트")
    void savePaymentTest() {
        // Given
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setReservation(reservation);
        payment.setOrderId("order123");
        payment.setAmount(10000);
        payment.setGoodsName("Test Goods");

        // When
        Payment savedPayment = paymentRepository.save(payment);

        // Then
        assertThat(savedPayment.getId()).isNotNull();
        assertThat(paymentRepository.findAll()).hasSize(1);
        assertThat(paymentRepository.findById(savedPayment.getId())).contains(savedPayment);
    }

    @Test
    @DisplayName("OrderId로 Payment 조회 테스트")
    void findByOrderIdTest() {
        // Given
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setReservation(reservation);
        payment.setOrderId("order123");
        payment.setAmount(10000);
        payment.setGoodsName("Test Goods");
        paymentRepository.save(payment);

        // When
        Optional<Payment> foundPayment = paymentRepository.findByOrderId("order123");

        // Then
        assertThat(foundPayment).isPresent();
        assertThat(foundPayment.get()).isEqualTo(payment);
    }

    @Test
    @DisplayName("User로 Payment 목록 조회 테스트")
    void findByUserTest() {
        // Given
        Payment payment1 = new Payment();
        payment1.setUser(user);
        payment1.setOrderId("order1");
        paymentRepository.save(payment1);

        Payment payment2 = new Payment();
        payment2.setUser(user);
        payment2.setOrderId("order2");
        paymentRepository.save(payment2);

        // When
        List<Payment> payments = paymentRepository.findByUser(user);

        // Then
        assertThat(payments).hasSize(2);
        assertThat(payments).extracting("orderId").containsExactlyInAnyOrder("order1", "order2");
    }

    @Test
    @DisplayName("Reservation ID로 Payment 목록 조회 테스트")
    void findByReservationIdTest() {
        // Given
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setReservation(reservation);
        payment.setOrderId("order123");
        paymentRepository.save(payment);

        // When
        List<Payment> payments = paymentRepository.findByReservationId(reservation.getId());

        // Then
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0)).isEqualTo(payment);
    }

    @Test
    @DisplayName("존재하지 않는 OrderId로 조회 시 빈 결과 반환")
    void findByOrderId_NotFound() {
        // When
        Optional<Payment> payment = paymentRepository.findByOrderId("nonexistentOrderId");

        // Then
        assertThat(payment).isEmpty();
    }

    @Test
    @DisplayName("다른 User의 Payment 조회 테스트")
    void findByUser_OtherUser() {
        // Given
        User otherUser = new User("otherUser", "other-uuid", "ROLE_USER");
        userRepository.save(otherUser);

        Payment payment = new Payment();
        payment.setUser(otherUser);
        payment.setOrderId("orderOther");
        paymentRepository.save(payment);

        // When
        List<Payment> payments = paymentRepository.findByUser(otherUser);

        // Then
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0)).isEqualTo(payment);

        // Original user should have no payments
        List<Payment> originalUserPayments = paymentRepository.findByUser(user);
        assertThat(originalUserPayments).isEmpty();
    }

    @Test
    @DisplayName("Reservation ID로 Payment 조회 시 예약이 없는 경우 빈 결과 반환")
    void findByReservationId_NoPayments() {
        // Given
        Long nonExistentReservationId = 999L;

        // When
        List<Payment> payments = paymentRepository.findByReservationId(nonExistentReservationId);

        // Then
        assertThat(payments).isEmpty();
    }
}
