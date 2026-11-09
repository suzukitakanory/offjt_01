package com.attendance.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AttendanceApiException.class)
    public ResponseEntity<ApiErrorResponse> handleAttendanceApiException(AttendanceApiException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(error(exception.getErrorCode(), exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception) {
        logger.error("Unexpected API error", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("ERR99001", "予期しないエラーが発生しました。システム管理者に連絡してください。"));
    }

    private ApiErrorResponse error(String code, String message) {
        return new ApiErrorResponse(code, message, OffsetDateTime.now(ZoneOffset.UTC));
    }
}
