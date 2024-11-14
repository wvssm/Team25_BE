package com.team25.backend.global.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ErrorCode {
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
    FAIL_LOGIN(HttpStatus.UNAUTHORIZED, "로그인을 실패했습니다.");

    private final HttpStatus status;
    private final String message;

    public HttpStatus getStatus() {
        return status;
    }
    public String getMessage() {
        return message;
    }
}
