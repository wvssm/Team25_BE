package com.team25.backend.domain.payment.service;

import com.team25.backend.domain.payment.dto.request.*;
import com.team25.backend.domain.payment.entity.BillingKey;
import com.team25.backend.domain.payment.entity.Payment;
import com.team25.backend.domain.payment.repository.BillingKeyRepository;
import com.team25.backend.domain.payment.repository.PaymentRepository;
import com.team25.backend.domain.reservation.repository.ReservationRepository;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.domain.user.repository.UserRepository;
import com.team25.backend.global.exception.CustomException;
import com.team25.backend.global.exception.ErrorCode;
import com.team25.backend.global.util.EncryptionUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
class PaymentServiceTest {

    private PaymentService paymentService;

    @Autowired
    private BillingKeyRepository billingKeyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @MockBean
    private RestClient restClient;

    @MockBean
    private EncryptionUtil encryptionUtil;

    private User user;
    private String userUuid;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("testUsername", "test-uuid", "ROLE_USER"));
        userUuid = user.getUuid();

        paymentService = new PaymentService(restClient, billingKeyRepository, userRepository, paymentRepository, reservationRepository, encryptionUtil);
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAllInBatch();
        billingKeyRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("이미 빌링키가 존재하는 경우 빌링키 발급 시 예외 발생")
    void createBillingKey_AlreadyExists() {
        // Given
        BillingKeyRequest requestDto = new BillingKeyRequest("testEncData", null);

        BillingKey existingBillingKey = new BillingKey();
        existingBillingKey.setUser(user);
        billingKeyRepository.save(existingBillingKey);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            paymentService.createBillingKey(userUuid, requestDto);
        });

        assertEquals(ErrorCode.BILLING_KEY_EXISTS, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자 정보가 없는 경우 빌링키 발급 시 예외 발생")
    void createBillingKey_UserNotFound() {
        // Given
        String invalidUserUuid = "invalid-uuid";
        BillingKeyRequest requestDto = new BillingKeyRequest("testEncData", null);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            paymentService.createBillingKey(invalidUserUuid, requestDto);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("빌링키가 없는 경우 빌링키 삭제 시 예외 발생")
    void expireBillingKey_BillingKeyNotFound() {
        // Given
        ExpireBillingKeyRequest requestDto = new ExpireBillingKeyRequest("testOrderId");

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            paymentService.expireBillingKey(userUuid, requestDto);
        });

        assertEquals(ErrorCode.BILLING_KEY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자 정보가 없는 경우 빌링키 존재 유무 확인 시 예외 발생")
    void billingKeyExists_UserNotFound() {
        // Given
        String invalidUserUuid = "invalid-uuid";
        userRepository.deleteAll();

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            paymentService.billingKeyExists(invalidUserUuid);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }


    @Test
    @DisplayName("빌링키 존재 여부 확인 - 빌링키가 없는 경우")
    void billingKeyExists_NoBillingKey() {
        // When
        Map<String, Object> response = paymentService.billingKeyExists(userUuid);

        // Then
        assertFalse((Boolean) response.get("exists"));
        assertEquals("", response.get("cardName"));
    }

    @Test
    @DisplayName("빌링키 존재 여부 확인 - 빌링키가 있는 경우")
    void billingKeyExists_BillingKeyExists() {
        // Given
        BillingKey billingKey = new BillingKey();
        billingKey.setUser(user);
        billingKey.setCardName("Test Card");
        billingKeyRepository.save(billingKey);

        // When
        Map<String, Object> response = paymentService.billingKeyExists(userUuid);

        // Then
        assertTrue((Boolean) response.get("exists"));
        assertEquals("Test Card", response.get("cardName"));
    }

    @Test
    @DisplayName("빌링키가 없는 경우 결제 요청 시 예외 발생")
    void requestPayment_BillingKeyNotFound() {
        // Given
        PaymentRequest requestDto = new PaymentRequest(1000, "Test Goods", null, false, null);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            paymentService.requestPayment(userUuid, requestDto);
        });

        assertEquals(ErrorCode.BILLING_KEY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자 정보가 없는 경우 결제 요청 시 예외 발생")
    void requestPayment_UserNotFound() throws Exception {
        String userUuid = "test-user-uuid";
        PaymentRequest requestDto = new PaymentRequest(1000, "Test Goods", null, false, null);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            paymentService.requestPayment(userUuid, requestDto);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("예약 정보가 유효하지 않은 경우 결제 요청 시 예외 발생")
    void requestPayment_InvalidReservation() {
        // Given
        BillingKey billingKey = new BillingKey();
        billingKey.setUser(user);
        billingKey.setBid("encrypted-bid");
        billingKeyRepository.save(billingKey);

        Long reservationId = 1L;
        PaymentRequest requestDto = new PaymentRequest(1000, "Test Goods", null, false, reservationId);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            paymentService.requestPayment(userUuid, requestDto);
        });

        assertEquals(ErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("결제 정보가 없는 경우 결제 취소 시 예외 발생")
    void requestCancel_PaymentNotFound() {
        // Given
        PaymentCancelRequest requestDto = new PaymentCancelRequest("testOrderId", "Test Reason");

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            paymentService.requestCancel(userUuid, requestDto);
        });

        assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("결제 정보와 사용자 정보가 일치하지 않는 경우 결제 취소 시 예외 발생")
    void requestCancel_UserMismatch() {
        // Given
        PaymentCancelRequest requestDto = new PaymentCancelRequest("testOrderId", "Test Reason");

        User otherUser = new User("otherUser", "other-uuid", "ROLE_USER");
        userRepository.save(otherUser);

        Payment payment = new Payment();
        payment.setUser(otherUser);
        payment.setOrderId(requestDto.orderId());
        paymentRepository.save(payment);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            paymentService.requestCancel(userUuid, requestDto);
        });

        assertEquals(ErrorCode.PAYMENT_USER_MISMATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("결제 정보가 없는 경우 단일 결제정보 조회 시 예외 발생")
    void getPaymentByOrderId_PaymentNotFound() {
        // Given
        String orderId = "testOrderId";

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            paymentService.getPaymentByOrderId(orderId);
        });

        assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("결제 정보가 있는 경우 단일 결제정보 조회")
    void getPaymentByOrderId_Success() {
        // Given
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setOrderId("testOrderId");
        paymentRepository.save(payment);

        // When
        var response = paymentService.getPaymentByOrderId("testOrderId");

        // Then
        assertNotNull(response);
        assertEquals("testOrderId", response.orderId());
    }

    @Test
    @DisplayName("사용자 정보가 없는 경우 결제정보 목록 조회 시 예외 발생")
    void getPaymentsByUserUuid_UserNotFound() {
        // Given
        String invalidUserUuid = "invalid-uuid";

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            paymentService.getPaymentsByUserUuid(invalidUserUuid);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자의 결제정보 목록 조회")
    void getPaymentsByUserUuid_Success() {
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
        var payments = paymentService.getPaymentsByUserUuid(userUuid);

        // Then
        assertEquals(2, payments.size());
    }

    @Test
    @DisplayName("예약 정보가 없는 경우 예약별 결제정보 목록 조회 시 빈 리스트 반환")
    void getPaymentsByReservationId_ReservationNotFound() {
        // Given
        Long reservationId = 1L;

        // When
        var payments = paymentService.getPaymentsByReservationId(reservationId);

        // Then
        assertTrue(payments.isEmpty());
    }
}
