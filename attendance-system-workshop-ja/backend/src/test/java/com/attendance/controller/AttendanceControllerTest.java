package com.attendance.controller;

import com.attendance.dto.AttendanceRequest;
import com.attendance.dto.AttendanceResponse;
import com.attendance.exception.AttendanceApiException;
import com.attendance.service.AttendanceService;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
    void saveLeaveAttendanceReturnsSavedLeaveRecordAndNullClockTimes() throws Exception {
        when(attendanceService.saveAttendance(any())).thenReturn(
                new AttendanceResponse(2L, "EMP001", "2025-01-15", null, null,
                        "PERSONAL_LEAVE", "私用のため休暇", "2025-01-15T09:00:00",
                        "2025-01-15T09:00:00"));

        mockMvc.perform(post("/api/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": "EMP001",
                                  "attendanceDate": "2025-01-15",
                                  "clockInTime": null,
                                  "clockOutTime": null,
                                  "status": "PERSONAL_LEAVE",
                                  "notes": "私用のため休暇"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PERSONAL_LEAVE"))
                .andExpect(jsonPath("$.notes").value("私用のため休暇"));

        ArgumentCaptor<AttendanceRequest> requestCaptor =
                ArgumentCaptor.forClass(AttendanceRequest.class);
        verify(attendanceService).saveAttendance(requestCaptor.capture());
        AttendanceRequest request = requestCaptor.getValue();
        assertNull(request.getClockInTime());
        assertNull(request.getClockOutTime());
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

    @Test
    void saveLeaveAttendanceReturnsConflictWhenDateHasOutOfScopeRecord() throws Exception {
        when(attendanceService.saveAttendance(any())).thenThrow(new AttendanceApiException(
                HttpStatus.CONFLICT,
                "ERR02001",
                "指定日には休暇登録の対象外となる勤怠レコードが存在します",
                Map.of("field", "status")));

        mockMvc.perform(post("/api/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": "EMP001",
                                  "attendanceDate": "2025-01-15",
                                  "clockInTime": null,
                                  "clockOutTime": null,
                                  "status": "SICK_LEAVE",
                                  "notes": null
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ERR02001"))
                .andExpect(jsonPath("$.details.field").value("status"));
    }

    @Test
    void deleteLeaveByDateReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/attendance/EMP001/2025-01-15"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(attendanceService).deleteLeaveByDate("EMP001", "2025-01-15");
    }

    @Test
    void deleteLeaveByDateReturnsNotFoundWhenRecordDoesNotExist() throws Exception {
        doThrow(new AttendanceApiException(
                HttpStatus.NOT_FOUND,
                "ERR02002",
                "指定された日付の休暇レコードが見つかりません",
                Map.of("field", "attendanceDate")))
                .when(attendanceService).deleteLeaveByDate("EMP001", "2025-01-15");

        mockMvc.perform(delete("/api/attendance/EMP001/2025-01-15"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ERR02002"))
                .andExpect(jsonPath("$.message").value("指定された日付の休暇レコードが見つかりません"));
    }

    @Test
    void deleteLeaveByDateReturnsConflictForOutOfScopeRecord() throws Exception {
        doThrow(new AttendanceApiException(
                HttpStatus.CONFLICT,
                "ERR02001",
                "指定日には休暇登録の対象外となる勤怠レコードが存在します",
                Map.of("field", "status")))
                .when(attendanceService).deleteLeaveByDate("EMP001", "2025-01-15");

        mockMvc.perform(delete("/api/attendance/EMP001/2025-01-15"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ERR02001"));
    }
}
