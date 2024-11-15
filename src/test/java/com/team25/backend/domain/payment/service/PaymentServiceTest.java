package com.team25.backend.domain.payment.service;

import com.team25.backend.domain.payment.dto.request.BillingKeyRequest;
import com.team25.backend.domain.payment.dto.request.ExpireBillingKeyRequest;
import com.team25.backend.domain.payment.dto.request.PaymentCancelRequest;
import com.team25.backend.domain.payment.dto.request.PaymentRequest;
import com.team25.backend.domain.payment.entity.BillingKey;
import com.team25.backend.domain.payment.entity.Payment;
import com.team25.backend.domain.payment.repository.BillingKeyRepository;
import com.team25.backend.domain.payment.repository.PaymentRepository;
import com.team25.backend.domain.reservation.repository.ReservationRepository;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.domain.user.repository.UserRepository;
import com.team25.backend.global.exception.PaymentErrorCode;
import com.team25.backend.global.exception.PaymentException;
import com.team25.backend.global.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentService paymentService;
    private RestClient restClient;
    private BillingKeyRepository billingKeyRepository;
    private UserRepository userRepository;
    private PaymentRepository paymentRepository;
    private ReservationRepository reservationRepository;
    private EncryptionUtil encryptionUtil;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        billingKeyRepository = mock(BillingKeyRepository.class);
        userRepository = mock(UserRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        encryptionUtil = mock(EncryptionUtil.class);

        paymentService = new PaymentService(restClient, billingKeyRepository, userRepository, paymentRepository, reservationRepository, encryptionUtil);
    }

    @Test
    @DisplayName("이미 빌링키가 존재하는 경우 빌링키 발급 시 예외 발생")
    void createBillingKey_AlreadyExists() {
        BillingKeyRequest requestDto = new BillingKeyRequest("testEncData", null);

        User user = new User();
        String userUuid = user.getUuid();

        when(userRepository.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(billingKeyRepository.findByUserUuid(userUuid)).thenReturn(Optional.of(new BillingKey()));

        PaymentException exception = assertThrows(PaymentException.class, () -> {
            paymentService.createBillingKey(userUuid, requestDto);
        });

        assertEquals(PaymentErrorCode.BILLING_KEY_EXISTS, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자 정보가 없는 경우 빌링키 발급 시 예외 발생")
    void createBillingKey_UserNotFound() {
        String userUuid = "test-user-uuid";
        BillingKeyRequest requestDto = new BillingKeyRequest("testEncData", null);

        when(userRepository.findByUuid(userUuid)).thenReturn(Optional.empty());

        PaymentException exception = assertThrows(PaymentException.class, () -> {
            paymentService.createBillingKey(userUuid, requestDto);
        });

        assertEquals(PaymentErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("빌링키가 없는 경우 빌링키 삭제 시 예외 발생")
    void expireBillingKey_BillingKeyNotFound() {
        String userUuid = "test-user-uuid";
        ExpireBillingKeyRequest requestDto = new ExpireBillingKeyRequest("testOrderId");

        when(billingKeyRepository.findByUserUuid(userUuid)).thenReturn(Optional.empty());

        PaymentException exception = assertThrows(PaymentException.class, () -> {
            paymentService.expireBillingKey(userUuid, requestDto);
        });

        assertEquals(PaymentErrorCode.BILLING_KEY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자 정보가 없는 경우 빌링키 존재 유무 확인 시 예외 발생")
    void billingKeyExists_UserNotFound() {
        String userUuid = "test-user-uuid";

        when(billingKeyRepository.findByUserUuid(userUuid)).thenReturn(Optional.empty());

        Map<String, Object> response = paymentService.billingKeyExists(userUuid);

        assertFalse((Boolean) response.get("exists"));
        assertEquals("", response.get("cardName"));
    }

    @Test
    @DisplayName("빌링키가 없는 경우 결제 요청 시 예외 발생")
    void requestPayment_BillingKeyNotFound() {
        String userUuid = "test-user-uuid";
        PaymentRequest requestDto = new PaymentRequest(1000, "Test Goods", null, false, null);

        when(billingKeyRepository.findByUserUuid(userUuid)).thenReturn(Optional.empty());

        PaymentException exception = assertThrows(PaymentException.class, () -> {
            paymentService.requestPayment(userUuid, requestDto);
        });

        assertEquals(PaymentErrorCode.BILLING_KEY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자 정보가 없는 경우 결제 요청 시 예외 발생")
    void requestPayment_UserNotFound() throws Exception {
        String userUuid = "test-user-uuid";
        PaymentRequest requestDto = new PaymentRequest(1000, "Test Goods", null, false, null);

        when(billingKeyRepository.findByUserUuid(userUuid)).thenReturn(Optional.of(new BillingKey()));
        when(userRepository.findByUuid(userUuid)).thenReturn(Optional.empty());

        PaymentException exception = assertThrows(PaymentException.class, () -> {
            paymentService.requestPayment(userUuid, requestDto);
        });

        assertEquals(PaymentErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("예약 정보가 유효하지 않은 경우 결제 요청 시 예외 발생")
    void requestPayment_InvalidReservation() throws Exception {
        String userUuid = "test-user-uuid";
        Long reservationId = 1L;
        PaymentRequest requestDto = new PaymentRequest(1000, "Test Goods", null, false, reservationId);

        when(billingKeyRepository.findByUserUuid(userUuid)).thenReturn(Optional.of(new BillingKey()));
        when(userRepository.findByUuid(userUuid)).thenReturn(Optional.of(new User()));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        PaymentException exception = assertThrows(PaymentException.class, () -> {
            paymentService.requestPayment(userUuid, requestDto);
        });

        assertEquals(PaymentErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("결제 정보가 없는 경우 결제 취소 시 예외 발생")
    void requestCancel_PaymentNotFound() throws Exception {
        String userUuid = "test-user-uuid";
        PaymentCancelRequest requestDto = new PaymentCancelRequest("testOrderId", "Test Reason");

        when(paymentRepository.findByOrderId(requestDto.orderId())).thenReturn(Optional.empty());

        PaymentException exception = assertThrows(PaymentException.class, () -> {
            paymentService.requestCancel(userUuid, requestDto);
        });

        assertEquals(PaymentErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("결제 정보와 사용자 정보가 일치하지 않는 경우 결제 취소 시 예외 발생")
    void requestCancel_UserMismatch() throws Exception {
        String userUuid = "test-user-uuid";
        PaymentCancelRequest requestDto = new PaymentCancelRequest("testOrderId", "Test Reason");

        User paymentUser = new User();

        Payment payment = new Payment();
        payment.setUser(paymentUser);

        when(paymentRepository.findByOrderId(requestDto.orderId())).thenReturn(Optional.of(payment));

        PaymentException exception = assertThrows(PaymentException.class, () -> {
            paymentService.requestCancel(userUuid, requestDto);
        });

        assertEquals(PaymentErrorCode.PAYMENT_USER_MISMATCH, exception.getErrorCode());
    }
}
