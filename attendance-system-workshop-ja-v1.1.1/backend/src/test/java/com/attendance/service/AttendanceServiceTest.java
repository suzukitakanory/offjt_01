package com.attendance.service;

import com.attendance.dto.AttendanceRequest;
import com.attendance.dto.AttendanceResponse;
import com.attendance.entity.AttendanceRecord;
import com.attendance.exception.AttendanceApiException;
import com.attendance.repository.AttendanceRecordRepository;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    void retrievesAttendanceByEmployeeAndDate() {
        AttendanceRecord record = sampleRecord();
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDate("EMP001", record.getAttendanceDate()))
                .thenReturn(java.util.Optional.of(record));

        AttendanceResponse response = attendanceService.getAttendanceByDate("EMP001", "2025-01-15").orElseThrow();

        assertEquals("09:00:00", response.getClockInTime());
        assertEquals("18:00:00", response.getClockOutTime());
    }

    @Test
    void rejectsInvalidEmployeeIdWhenRetrievingAttendance() {
        AttendanceApiException exception = org.junit.jupiter.api.Assertions.assertThrows(
                AttendanceApiException.class, () -> attendanceService.getAttendanceByDate("BAD", "2025-01-15"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("ERR03004", exception.getErrorCode());
        verifyNoInteractions(attendanceRecordRepository);
    }

    @Test
    void listsAttendanceWithinInclusiveDateRange() {
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDateBetween(
                "EMP001", java.time.LocalDate.parse("2025-01-01"), java.time.LocalDate.parse("2025-01-31")))
                .thenReturn(java.util.List.of(sampleRecord()));

        var records = attendanceService.getAttendancesByEmployeeId("EMP001", "2025-01-01", "2025-01-31");

        assertEquals(1, records.size());
        verify(attendanceRecordRepository).findByEmployeeIdAndAttendanceDateBetween(
                "EMP001", java.time.LocalDate.parse("2025-01-01"), java.time.LocalDate.parse("2025-01-31"));
    }

    @Test
    void returnsEmptyListForReversedDateRange() {
        var records = attendanceService.getAttendancesByEmployeeId("EMP001", "2025-01-31", "2025-01-01");

        assertTrue(records.isEmpty());
        verifyNoInteractions(attendanceRecordRepository);
    }

    private AttendanceRecord sampleRecord() {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(1L);
        record.setEmployeeId("EMP001");
        record.setAttendanceDate(java.time.LocalDate.parse("2025-01-15"));
        record.setClockInTime(java.time.LocalTime.parse("09:00"));
        record.setClockOutTime(java.time.LocalTime.parse("18:00"));
        record.setStatus("WORK");
        return record;
    }
}
