package com.team25.backend.global.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ErrorCode {
    // 예약 에러 코드
    NOT_MANAGER(HttpStatus.BAD_REQUEST,"매니저 회원이 아닙니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약이 존재하지 않습니다."),
    USER_HAS_NO_RESERVATIONS(HttpStatus.NOT_FOUND, "해당 회원은 예약 사항이 없습니다."),
    RESERVATION_NOT_BELONG_TO_USER(HttpStatus.BAD_REQUEST, "해당 회원의 예약 번호가 아닙니다."),
    MANAGER_REQUIRED(HttpStatus.BAD_REQUEST, "매니저 선택은 필수 값입니다."),
    RESERVATION_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "이미 취소된 예약입니다"),
    INVALID_CANCEL_REASON(HttpStatus.BAD_REQUEST,"올바르지 않은 변심 이유입니다."),
    CANCEL_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "변심 이유를 반드시 선택해야 합니다."),
    INVALID_DATETIME_FORMAT(HttpStatus.BAD_REQUEST, "날짜 시간 형식이 올바르지 않습니다."),
    RESERVATION_WITHOUT_REPORT(HttpStatus.NOT_FOUND, "해당 예약에 대한 리포트가 없습니다."),
    RESERVATION_WITHOUT_ACCOMPANY(HttpStatus.NOT_FOUND, "해당 예약에 대한 실시간 동행 현황 정보가 없습니다."),
    INVALID_SERVICE_TYPE(HttpStatus.BAD_REQUEST,"유효하지 않은 서비스 타입입니다."),
    INVALID_TRANSPORTATION_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 이동 수단입니다."),
    INVALID_RESERVATION_STATUS(HttpStatus.BAD_REQUEST,"유효하지 않은 예약 상태입니다."),
    INVALID_DEPARTRUE_ADDRESS(HttpStatus.BAD_REQUEST,"유효하지 않는 출발 장소입니다."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST,"유효하지 않은 가격입니다."),
    PATIENT_REQUIRED(HttpStatus.BAD_REQUEST,"환자 정보는 필수 값입니다."),
    INVALID_PATIENT_NAME(HttpStatus.BAD_REQUEST,"유효하지 않은 환자명입니다."),
    INVALID_PATIENT_PHONE(HttpStatus.BAD_REQUEST,"유효하지 않은 번호입니다."),
    INVALID_PATIENT_GENDER(HttpStatus.BAD_REQUEST,"유효하지 않은 성별입니다."),
    INVALID_PATIENT_RELATION(HttpStatus.BAD_REQUEST,"유효하지 않은 보호자 관계입니다."),
    INVALID_PATIENT_BIRTHDATE(HttpStatus.BAD_REQUEST,"유효하지 않은 환자 생년월일입니다."),
    INVALID_NOK_PHONE(HttpStatus.BAD_REQUEST,"유효하지 않은 보호자 번호입니다."),
    INVALID_ARRIVAL_ADDRESS(HttpStatus.BAD_REQUEST,"유효하지 않는 도착 장소입니다."),

    // 리포트 에러 코드
    REQUIRED_DOCTOR_SUMMARY_MISSING(HttpStatus.BAD_REQUEST, "의사 소견은 1글자 이상이어야 합니다."),
    REQUIRED_FREQUENCY_MISSING(HttpStatus.BAD_REQUEST, "복약 횟수는 필수 입력값입니다."),
    INVALID_FREQUENCY(HttpStatus.BAD_REQUEST, "유효하지 않은 복약 횟수입니다."),
    INVALID_MEDICINE_TIME(HttpStatus.BAD_REQUEST, "복약 시간은 식전 30분, 식후 30분, 식간이어야합니다."),
    REQUIRED_TIME_OF_DAYS_MISSING(HttpStatus.BAD_REQUEST, "복약은 아침, 점심, 저녁 등으로 설정하여야 하고 1글자 이상이어야합니다."),


    // 실시간 동행 에러 코드
    INVALID_ACCOMPANY_STATUS(HttpStatus.BAD_REQUEST, "올바르지 않은 실시간 동행 진행 상태입니다."),
    INVALID_LATITUDE(HttpStatus.BAD_REQUEST, "위도는 0이상 90 이하의 값입니다."),
    INVALID_LONGITUDE(HttpStatus.BAD_REQUEST, "경도는 0이상 180 이하의 값입니다."),
    REQUIRED_DATE_MISSING(HttpStatus.BAD_REQUEST, "시간은 필수 입력 값입니다."),
    REQUIRED_DESCRIPTION_MISSING(HttpStatus.BAD_REQUEST, "리포트 상세 사항은 필수 사항입니다."),


    KAKAO_PLATFORM_ERROR(HttpStatus.BAD_REQUEST, "KAKAO PLATFORM INTERNAL ERROR"),
    KAKAO_TOKEN_FORMAT_ERROR(HttpStatus.BAD_REQUEST, "BAD REQUEST FORMAT"),
    KAKAO_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN IS EXPIRED OR INVALID"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"회원이 존재하지 않습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT,"회원이 이미 존재합니다."),
    RESPONSE_BODY_NULL(HttpStatus.INTERNAL_SERVER_ERROR,"API 요청 응답이 비어있습니다."),
    MANAGER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 매니저입니다."),
    MANAGER_ALREADY_EXISTS(HttpStatus.CONFLICT,"매니저 정보가 이미 등록되어 있습니다."),
    TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "JWT 토큰이 만료되었습니다."),
    INVALID_FORMAT_TOKEN(HttpStatus.BAD_REQUEST, "잘못된 형식의 JWT 토큰입니다."),
    NOT_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "해당 JWT 토큰은 Refresh 토큰이 아닙니다."),
    NOT_EXISTED_REFRESH_TOKEN(HttpStatus.BAD_REQUEST,"해당 Refresh 토큰이 존재하지 않습니다."),
    FAIL_LOGIN(HttpStatus.UNAUTHORIZED, "로그인을 실패했습니다."),

    // 결제 에러 코드
    BILLING_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "빌링키를 찾을 수 없습니다."),
    BILLING_KEY_EXISTS(HttpStatus.CONFLICT, "이미 빌링키가 존재합니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제를 찾을 수 없습니다."),
    PAYMENT_USER_MISMATCH(HttpStatus.FORBIDDEN, "결제 사용자와 요청 사용자가 일치하지 않습니다."),
    FAILED_TO_REQUEST_BILLING_KEY(HttpStatus.INTERNAL_SERVER_ERROR, "빌링키 요청에 실패했습니다."),
    FAILED_TO_PROCESS_PAYMENT(HttpStatus.INTERNAL_SERVER_ERROR, "결제 처리에 실패했습니다."),
    FAILED_TO_EXPIRE_BILLING_KEY(HttpStatus.INTERNAL_SERVER_ERROR, "빌링키 삭제에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    public HttpStatus getStatus() {
        return status;
    }
    public String getMessage() {
        return message;
    }
}
