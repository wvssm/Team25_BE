package com.team25.backend.global.exception;

import com.team25.backend.global.dto.response.ApiResponse;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.team25.backend.global.exception.ErrorCode.INVALID_FORMAT_TOKEN;
import static com.team25.backend.global.exception.ErrorCode.TOKEN_EXPIRED;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(new ApiResponse<>(false, "유효성 검사 실패", errors),
            HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ManagerException.class)
    public ResponseEntity<ApiResponse<String>> handleManagerException(ManagerException ex) {
        return new ResponseEntity<>(
            ApiResponse.<String>builder()
                .status(false)
                .message(ex.getErrorCode().getMessage())
                .data(null)
                .build(),
            ex.getErrorCode().getHttpStatus()
        );
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<String>> handleCustomException(CustomException ex) {
        ApiResponse<String>apiResponse = new ApiResponse<>(false,ex.getErrorCode().getMessage(),null);
        return ResponseEntity.status(ex.getErrorCode().getStatus())
                .body(apiResponse);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<String>> handleExpiredJwtException(ExpiredJwtException ex) {
        ApiResponse<String>apiResponse = new ApiResponse<>(false,TOKEN_EXPIRED.getMessage(),null);
        return ResponseEntity.status(TOKEN_EXPIRED.getStatus())
                .body(apiResponse);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ApiResponse<String>> handleMalformedJwtException(MalformedJwtException ex) {
        ApiResponse<String>apiResponse = new ApiResponse<>(false,INVALID_FORMAT_TOKEN.getMessage(),null);
        return ResponseEntity.status(INVALID_FORMAT_TOKEN.getStatus())
                .body(apiResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex) {
        ApiResponse<String>apiResponse = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(apiResponse);
    }
}