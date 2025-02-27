package com.team25.backend.domain.payment.controller;

import com.team25.backend.global.annotation.LoginUser;
import com.team25.backend.domain.payment.dto.request.BillingKeyRequest;
import com.team25.backend.domain.payment.dto.request.PaymentCancelRequest;
import com.team25.backend.domain.payment.dto.request.PaymentRequest;
import com.team25.backend.domain.payment.dto.request.ExpireBillingKeyRequest;
import com.team25.backend.domain.payment.dto.response.BillingKeyResponse;
import com.team25.backend.domain.payment.dto.response.ExpireBillingKeyResponse;
import com.team25.backend.domain.payment.dto.response.PaymentInfoResponse;
import com.team25.backend.domain.payment.dto.response.PaymentResponse;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.global.dto.response.ApiResponse;
import com.team25.backend.domain.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // 빌링키 발급
    @PostMapping("/billing-key")
    public ResponseEntity<ApiResponse<BillingKeyResponse>> createBillingKey(@LoginUser User user, @RequestBody BillingKeyRequest requestDto) throws Exception {
        String userUuid = user.getUuid(); // 현재 사용자 식별자 가져오기
        BillingKeyResponse responseDto = paymentService.createBillingKey(userUuid, requestDto);
        return new ResponseEntity<>(
                new ApiResponse<>(true, "빌링키 발급을 성공했습니다.", responseDto), HttpStatus.OK
        );
    }

    // 결제 요청
    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> payment(@LoginUser User user, @RequestBody PaymentRequest requestDto) throws Exception {
        String userUuid = user.getUuid();
        PaymentResponse responseDto = paymentService.requestPayment(userUuid, requestDto);
        return new ResponseEntity<>(
                new ApiResponse<>(true, responseDto.resultMsg(), responseDto), HttpStatus.OK
        );
    }

    // 결제 취소
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancel(@LoginUser User user, @RequestBody PaymentCancelRequest requestDto) throws Exception {
        String userUuid = user.getUuid();
        PaymentResponse responseDto = paymentService.requestCancel(userUuid, requestDto);
        return new ResponseEntity<>(
                new ApiResponse<>(true, responseDto.resultMsg(), responseDto), HttpStatus.OK
        );
    }

    // 빌링키 삭제
    @PostMapping("/billing-key/expire")
    public ResponseEntity<ApiResponse<ExpireBillingKeyResponse>> expireBillingKey(@LoginUser User user, @RequestBody ExpireBillingKeyRequest requestDto) throws Exception {
        String userUuid = user.getUuid();
        ExpireBillingKeyResponse responseDto = paymentService.expireBillingKey(userUuid, requestDto);
        return new ResponseEntity<>(
                new ApiResponse<>(true, responseDto.resultMsg(), responseDto), HttpStatus.OK
        );
    }

    // 빌링키 존재 유무 확인
    @GetMapping("/billing-key/exists")
    public ResponseEntity<ApiResponse<Map<String, Object>>> billingKeyExists(@LoginUser User user) {
        String userUuid = user.getUuid();
        Map<String, Object> billingKeyInfo = paymentService.billingKeyExists(userUuid);

        String message = (boolean) billingKeyInfo.get("exists") ?
                "성공적으로 빌링키 정보를 가져왔습니다." :
                "성공적으로 빌링키 존재 유무를 가져왔습니다.";

        return new ResponseEntity<>(
                new ApiResponse<>(true, message, billingKeyInfo), HttpStatus.OK
        );
    }

    // orderId로 단일 결제정보 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<PaymentInfoResponse>> getPaymentByOrderId(@PathVariable String orderId) {
        PaymentInfoResponse responseDto = paymentService.getPaymentByOrderId(orderId);
        return new ResponseEntity<>(
                new ApiResponse<>(true, "성공적으로 결제 정보를 가져왔습니다.", responseDto), HttpStatus.OK
        );
    }

    // 해당 유저의 결제정보 목록 조회
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<PaymentInfoResponse>>> getPaymentsByUser(@LoginUser User user) {
        String userUuid = user.getUuid();
        List<PaymentInfoResponse> responseDtos = paymentService.getPaymentsByUserUuid(userUuid);
        return new ResponseEntity<>(
                new ApiResponse<>(true, "성공적으로 결제 정보 목록을 가져왔습니다.", responseDtos), HttpStatus.OK
        );
    }

    // 해당 예약의 결제정보 목록 조회
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<ApiResponse<List<PaymentInfoResponse>>> getPaymentsByReservation(@PathVariable Long reservationId) {
        List<PaymentInfoResponse> responseDtos = paymentService.getPaymentsByReservationId(reservationId);
        return new ResponseEntity<>(
                new ApiResponse<>(true, "성공적으로 결제 정보 목록을 가져왔습니다.", responseDtos), HttpStatus.OK
        );
    }
}
