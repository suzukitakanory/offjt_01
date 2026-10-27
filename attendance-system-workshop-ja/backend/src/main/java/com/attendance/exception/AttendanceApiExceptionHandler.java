package com.attendance.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class AttendanceApiExceptionHandler {

    @ExceptionHandler(AttendanceApiException.class)
    public ResponseEntity<Map<String, Object>> handleAttendanceApiException(
            AttendanceApiException exception) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("error", exception.getError());
        body.put("message", exception.getMessage());
        body.put("timestamp", Instant.now().toString());
        if (!exception.getDetails().isEmpty()) {
            body.put("details", exception.getDetails());
        }
        return ResponseEntity.status(exception.getStatus()).body(body);
    }
}
