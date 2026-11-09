package com.attendance.controller;

import com.attendance.dto.AttendanceResponse;
import com.attendance.service.AttendanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
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
                .andExpect(status().isOk());
    }

    @Test
    void testGetAttendanceByDate() throws Exception {
        when(attendanceService.getAttendanceByDate("EMP001", "2025-01-15")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/attendance/EMP001/2025-01-15"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAttendancesByEmployeeId() throws Exception {
        mockMvc.perform(get("/api/attendance/EMP001")).andExpect(status().isOk());
    }
}
