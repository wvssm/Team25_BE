package com.team25.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PaymentErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    BILLING_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "빌링키를 찾을 수 없습니다."),
    BILLING_KEY_EXISTS(HttpStatus.CONFLICT, "이미 빌링키가 존재합니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제를 찾을 수 없습니다."),
    PAYMENT_USER_MISMATCH(HttpStatus.FORBIDDEN, "결제 사용자와 요청 사용자가 일치하지 않습니다."),
    FAILED_TO_REQUEST_BILLING_KEY(HttpStatus.INTERNAL_SERVER_ERROR, "빌링키 요청에 실패했습니다."),
    FAILED_TO_PROCESS_PAYMENT(HttpStatus.INTERNAL_SERVER_ERROR, "결제 처리에 실패했습니다."),
    FAILED_TO_EXPIRE_BILLING_KEY(HttpStatus.INTERNAL_SERVER_ERROR, "빌링키 삭제에 실패했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
