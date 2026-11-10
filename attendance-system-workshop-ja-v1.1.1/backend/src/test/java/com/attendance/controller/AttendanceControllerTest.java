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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 勤怠コントローラーテスト
 * 関連要件: REQ-UC002 - カレンダー操作, REQ-UC003 - 勤務時間入力, REQ-UC004 - 休暇登録
 */
@WebMvcTest(AttendanceController.class)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    @Test
    void testSaveAttendance() throws Exception {
        when(attendanceService.saveAttendance(any())).thenReturn(
                new AttendanceResponse(1L, "EMP001", "2025-01-15", "09:00:00", "18:00:00",
                        "WORK", null, null, null));
        mockMvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":\"EMP001\",\"attendanceDate\":\"2025-01-15\","
                                + "\"clockInTime\":\"09:00\",\"clockOutTime\":\"18:00\",\"status\":\"WORK\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value("EMP001"));
    }

    @Test
    void testSaveAttendanceReturnsContractErrorForInvalidInput() throws Exception {
        mockMvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":\"EMP001\",\"attendanceDate\":\"2025-01-15\","
                                + "\"clockInTime\":\"9:00\",\"clockOutTime\":\"18:00\",\"status\":\"WORK\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ERR03002"))
                .andExpect(jsonPath("$.message").value("有効な時刻形式で入力してください"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testGetAttendanceByDate() throws Exception {
        when(attendanceService.getAttendanceByDate("EMP001", "2025-01-15"))
                .thenReturn(Optional.of(new AttendanceResponse(
                        1L, "EMP001", "2025-01-15", "09:00:00", "18:00:00", "WORK", null, null, null)));
        mockMvc.perform(get("/api/attendance/EMP001/2025-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendanceDate").value("2025-01-15"));
    }

    @Test
    void testGetAttendanceByDateReturnsNotFound() throws Exception {
        when(attendanceService.getAttendanceByDate("EMP001", "2025-01-15")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/attendance/EMP001/2025-01-15"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ERR02001"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testGetAttendanceByDateReturnsBadRequestForInvalidEmployeeId() throws Exception {
        when(attendanceService.getAttendanceByDate("BAD", "2025-01-15")).thenThrow(
                new AttendanceApiException(HttpStatus.BAD_REQUEST, "ERR03004", "従業員IDの形式が不正です"));
        mockMvc.perform(get("/api/attendance/BAD/2025-01-15"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ERR03004"));
    }

    @Test
    void testGetAttendancesByEmployeeId() throws Exception {
        when(attendanceService.getAttendancesByEmployeeId("EMP001", "2025-01-01", "2025-01-31"))
                .thenReturn(List.of());
        mockMvc.perform(get("/api/attendance/EMP001")
                        .queryParam("startDate", "2025-01-01")
                        .queryParam("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        verify(attendanceService).getAttendancesByEmployeeId("EMP001", "2025-01-01", "2025-01-31");
    }

    @Test
    void testGetAttendancesByEmployeeIdReturnsBadRequestForInvalidDate() throws Exception {
        when(attendanceService.getAttendancesByEmployeeId("EMP001", "invalid", null)).thenThrow(
                new AttendanceApiException(HttpStatus.BAD_REQUEST, "ERR03005", "勤怠日の形式が不正です"));
        mockMvc.perform(get("/api/attendance/EMP001").queryParam("startDate", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ERR03005"));
    }
}
