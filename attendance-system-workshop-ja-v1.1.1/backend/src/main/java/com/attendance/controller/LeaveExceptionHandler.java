package com.attendance.controller;

import com.attendance.dto.ApiErrorResponse;
import com.attendance.dto.CreateLeaveRequest;
import com.attendance.dto.UpdateLeaveRequest;
import com.attendance.exception.LeaveApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 休暇 API の入力不足、未対応区分、同日重複、不存在をメッセージ ID の応答へ変換する。
 */
@RestControllerAdvice
public class LeaveExceptionHandler {

    private static final List<String> PRIORITY = List.of(
            ApiErrorResponse.ERR03004,
            ApiErrorResponse.ERR03005,
            ApiErrorResponse.ERR03006,
            ApiErrorResponse.ERR03007,
            ApiErrorResponse.ERR03008,
            ApiErrorResponse.ERR02002,
            ApiErrorResponse.ERR02003,
            ApiErrorResponse.ERR02005);

    private static final Set<String> KNOWN_IDS = Set.copyOf(PRIORITY);

    @ExceptionHandler(LeaveApiException.class)
    public ResponseEntity<ApiErrorResponse> handleLeaveApiException(LeaveApiException exception) {
        return response(exception.getMessageId());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidLeaveRequest(MethodArgumentNotValidException exception)
            throws MethodArgumentNotValidException {
        Object target = exception.getBindingResult().getTarget();
        if (!(target instanceof CreateLeaveRequest) && !(target instanceof UpdateLeaveRequest)) {
            throw exception;
        }
        Set<String> found = exception.getBindingResult().getFieldErrors().stream()
                .map(this::messageId)
                .collect(Collectors.toSet());
        String messageId = PRIORITY.stream()
                .filter(found::contains)
                .findFirst()
                .orElse(ApiErrorResponse.ERR03004);
        return response(messageId);
    }

    private String messageId(FieldError error) {
        String code = error.getDefaultMessage();
        if (code != null && KNOWN_IDS.contains(code)) {
            return code;
        }
        return switch (error.getField()) {
            case "leaveType" -> ApiErrorResponse.ERR03005;
            case "memo" -> ApiErrorResponse.ERR03007;
            case "from", "to" -> ApiErrorResponse.ERR03008;
            default -> ApiErrorResponse.ERR03004;
        };
    }

    private ResponseEntity<ApiErrorResponse> response(String messageId) {
        HttpStatus status = switch (messageId) {
            case ApiErrorResponse.ERR02002 -> HttpStatus.NOT_FOUND;
            case ApiErrorResponse.ERR02003, ApiErrorResponse.ERR02005 -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiErrorResponse.of(messageId));
    }
}
