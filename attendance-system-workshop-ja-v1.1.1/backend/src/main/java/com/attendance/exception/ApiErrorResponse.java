package com.attendance.exception;

import java.time.OffsetDateTime;

public record ApiErrorResponse(String error, String message, OffsetDateTime timestamp) {
}
