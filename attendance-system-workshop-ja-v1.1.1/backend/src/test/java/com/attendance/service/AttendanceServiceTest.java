package com.attendance.service;

import com.attendance.dto.AttendanceRequest;
import com.attendance.dto.AttendanceResponse;
import com.attendance.repository.AttendanceRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 勤怠サービステスト
 * 関連要件: REQ-UC002 - カレンダー操作, REQ-UC003 - 勤務時間入力, REQ-UC004 - 休暇登録
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void savesNewWorkTimeWithSecondsSetToZero() {
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                "EMP001", java.time.LocalDate.parse("2025-01-15"))).thenReturn(java.util.Optional.empty());
        when(attendanceRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.saveAttendance(
                new AttendanceRequest("EMP001", "2025-01-15", "09:00", "18:00", "WORK", null));

        assertEquals("09:00:00", response.getClockInTime());
        assertEquals("18:00:00", response.getClockOutTime());
        assertEquals("WORK", response.getStatus());
    }

    @Test
    void testGetAttendanceByDate() {
        // TODO: REQ-UC003 - 特定日勤怠レコード取得ロジックのテスト
        assertTrue(true);
    }

    @Test
    void testGetAttendancesByEmployeeId() {
        // TODO: REQ-UC002 - 勤怠レコード一覧取得ロジックのテスト
        assertTrue(true);
    }
}
