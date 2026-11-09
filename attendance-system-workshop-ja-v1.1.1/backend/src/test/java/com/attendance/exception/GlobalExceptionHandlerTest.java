package com.attendance.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsAttendanceErrorsToTheirHttpStatusAndPayload() {
        AttendanceApiException exception = new AttendanceApiException(
                HttpStatus.BAD_REQUEST, "ERR03001", "出勤時刻は退勤時刻より前である必要があります");

        ResponseEntity<ApiErrorResponse> response = handler.handleAttendanceApiException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ERR03001", response.getBody().error());
        assertEquals("出勤時刻は退勤時刻より前である必要があります", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void mapsMissingAttendanceToNotFound() {
        AttendanceApiException exception = new AttendanceApiException(
                HttpStatus.NOT_FOUND, "ERR02001", "指定した日付の勤怠レコードはありません");

        ResponseEntity<ApiErrorResponse> response = handler.handleAttendanceApiException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("ERR02001", response.getBody().error());
    }

    @Test
    void mapsUnexpectedErrorsWithoutExposingInternalDetails() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleUnexpectedException(new IllegalStateException("internal detail"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("ERR99001", response.getBody().error());
        assertEquals("予期しないエラーが発生しました。システム管理者に連絡してください。",
                response.getBody().message());
    }
}
