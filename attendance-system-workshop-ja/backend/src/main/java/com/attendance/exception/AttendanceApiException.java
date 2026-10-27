package com.attendance.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class AttendanceApiException extends RuntimeException {

    private final HttpStatus status;
    private final String error;
    private final Map<String, Object> details;

    public AttendanceApiException(HttpStatus status, String error, String message,
                                  Map<String, Object> details) {
        super(message);
        this.status = status;
        this.error = error;
        this.details = details;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
