package com.attendance.controller;

import com.attendance.dto.AttendanceResponse;
import com.attendance.exception.AttendanceApiException;
import com.attendance.service.AttendanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceController.class)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    @Test
    void saveAttendanceReturnsSavedRecord() throws Exception {
        when(attendanceService.saveAttendance(any())).thenReturn(
                new AttendanceResponse(1L, "EMP001", "2025-01-15", "09:00:00", "18:00:00",
                        "WORK", null, "2025-01-15T09:00:00", "2025-01-15T18:00:00"));

        mockMvc.perform(post("/api/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": "EMP001",
                                  "attendanceDate": "2025-01-15",
                                  "clockInTime": "09:00",
                                  "clockOutTime": "18:00",
                                  "status": "WORK"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value("EMP001"))
                .andExpect(jsonPath("$.clockInTime").value("09:00:00"));
    }

    @Test
    void getAttendanceByDateReturnsExistingRecord() throws Exception {
        when(attendanceService.getAttendanceByDate("EMP001", "2025-01-15")).thenReturn(
                Optional.of(new AttendanceResponse(1L, "EMP001", "2025-01-15",
                        "09:00:00", "18:00:00", "WORK", null, null, null)));

        mockMvc.perform(get("/api/attendance/EMP001/2025-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clockOutTime").value("18:00:00"));
    }

    @Test
    void getAttendanceByDateReturnsNotFoundWhenRecordDoesNotExist() throws Exception {
        when(attendanceService.getAttendanceByDate("EMP001", "2025-01-15"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/attendance/EMP001/2025-01-15"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("指定された日付の勤怠レコードが見つかりません"));
    }

    @Test
    void saveAttendanceReturnsStructuredValidationError() throws Exception {
        when(attendanceService.saveAttendance(any())).thenThrow(new AttendanceApiException(
                HttpStatus.BAD_REQUEST, "ERR03002", "有効な時刻形式で入力してください",
                Map.of("field", "clockInTime", "rejectedValue", "25:00")));

        mockMvc.perform(post("/api/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": "EMP001",
                                  "attendanceDate": "2025-01-15",
                                  "clockInTime": "25:00",
                                  "clockOutTime": "18:00",
                                  "status": "WORK"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ERR03002"))
                .andExpect(jsonPath("$.message").value("有効な時刻形式で入力してください"))
                .andExpect(jsonPath("$.details.field").value("clockInTime"));
    }
}
