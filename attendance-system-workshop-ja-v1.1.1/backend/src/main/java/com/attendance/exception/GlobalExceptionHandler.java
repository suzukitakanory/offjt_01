package com.attendance.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Comparator;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldErrors().stream()
                .min(Comparator.comparingInt(this::validationPriority))
                .orElse(null);
        if (fieldError == null) {
            return badRequest("ERR03003", "出勤時刻と退勤時刻を入力してください");
        }
        String field = fieldError.getField();
        if ("employeeId".equals(field)) {
            return badRequest("ERR03004", "従業員IDの形式が不正です");
        }
        if ("attendanceDate".equals(field)) {
            return badRequest("ERR03005", "勤怠日の形式が不正です");
        }
        if ("clockInTime".equals(field) || "clockOutTime".equals(field)) {
            if ("NotBlank".equals(fieldError.getCode())) {
                return badRequest("ERR03003", "出勤時刻と退勤時刻を入力してください");
            }
            return badRequest("ERR03002", "有効な時刻形式で入力してください");
        }
        return badRequest("ERR03003", "出勤時刻と退勤時刻を入力してください");
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

    private ResponseEntity<ApiErrorResponse> badRequest(String code, String message) {
        return ResponseEntity.badRequest().body(error(code, message));
    }

    private int validationPriority(FieldError error) {
        return switch (error.getField()) {
            case "employeeId" -> 0;
            case "attendanceDate" -> 1;
            case "clockInTime", "clockOutTime" ->
                    "NotBlank".equals(error.getCode()) ? 2 : 3;
            default -> 4;
        };
    }
}
