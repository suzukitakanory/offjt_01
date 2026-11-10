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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
                "EMP001", LocalDate.parse("2099-12-31"))).thenReturn(Optional.empty());
        when(attendanceRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.saveAttendance(
                new AttendanceRequest("EMP001", "2099-12-31", "09:00", "18:00", "WORK", null));

        assertEquals("2099-12-31", response.getAttendanceDate());
        assertEquals("09:00:00", response.getClockInTime());
        assertEquals("18:00:00", response.getClockOutTime());
        assertEquals("WORK", response.getStatus());
    }

    @Test
    void updatesExistingRecordWithoutChangingNotesOrCreationTime() {
        AttendanceRecord existing = sampleRecord();
        existing.setNotes("preserved");
        existing.setCreatedAt(LocalDateTime.parse("2025-01-01T10:00:00"));
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                "EMP001", existing.getAttendanceDate())).thenReturn(Optional.of(existing));
        when(attendanceRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.saveAttendance(
                new AttendanceRequest("EMP001", "2025-01-15", "08:30", "17:30", "WORK", null));

        assertEquals("08:30:00", response.getClockInTime());
        assertEquals("preserved", response.getNotes());
        assertEquals("2025-01-01T10:00", response.getCreatedAt());
        verify(attendanceRecordRepository).save(existing);
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void rejectsInvalidSaveRequestsWithoutAccessingRepository(
            AttendanceRequest request, String errorCode) {
        AttendanceApiException exception = assertThrows(
                AttendanceApiException.class, () -> attendanceService.saveAttendance(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(errorCode, exception.getErrorCode());
        verifyNoInteractions(attendanceRecordRepository);
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> invalidRequests() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(request("BAD", "2025-01-15", "09:00", "18:00"),
                        "ERR03004"),
                org.junit.jupiter.params.provider.Arguments.of(request("EMP001", "2025-02-30", "09:00", "18:00"),
                        "ERR03005"),
                org.junit.jupiter.params.provider.Arguments.of(request("EMP001", "2025-01-15", "", "18:00"),
                        "ERR03003"),
                org.junit.jupiter.params.provider.Arguments.of(request("EMP001", "2025-01-15", "9:00", "18:00"),
                        "ERR03002"),
                org.junit.jupiter.params.provider.Arguments.of(request("EMP001", "2025-01-15", "18:00", "18:00"),
                        "ERR03001"));
    }

    private static AttendanceRequest request(String employeeId, String date, String clockIn, String clockOut) {
        return new AttendanceRequest(employeeId, date, clockIn, clockOut, "WORK", null);
    }

    @Test
    void retrievesAttendanceByEmployeeAndDate() {
        AttendanceRecord record = sampleRecord();
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDate("EMP001", record.getAttendanceDate()))
                .thenReturn(Optional.of(record));

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
    void returnsEmptyWhenAttendanceIsNotRegistered() {
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                "EMP001", LocalDate.parse("2025-01-15"))).thenReturn(Optional.empty());

        assertTrue(attendanceService.getAttendanceByDate("EMP001", "2025-01-15").isEmpty());
    }

    @Test
    void listsAttendanceWithinInclusiveDateRange() {
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDateBetween(
                "EMP001", LocalDate.parse("2025-01-01"), LocalDate.parse("2025-01-31")))
                .thenReturn(List.of(sampleRecord()));

        var records = attendanceService.getAttendancesByEmployeeId("EMP001", "2025-01-01", "2025-01-31");

        assertEquals(1, records.size());
        verify(attendanceRecordRepository).findByEmployeeIdAndAttendanceDateBetween(
                "EMP001", LocalDate.parse("2025-01-01"), LocalDate.parse("2025-01-31"));
    }

    @Test
    void listsAttendanceWhenOnlyOneDateBoundIsProvided() {
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDateGreaterThanEqual(
                "EMP001", LocalDate.parse("2025-01-15"))).thenReturn(List.of());
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDateLessThanEqual(
                "EMP001", LocalDate.parse("2025-01-15"))).thenReturn(List.of());

        attendanceService.getAttendancesByEmployeeId("EMP001", "2025-01-15", null);
        attendanceService.getAttendancesByEmployeeId("EMP001", null, "2025-01-15");

        verify(attendanceRecordRepository).findByEmployeeIdAndAttendanceDateGreaterThanEqual(
                "EMP001", LocalDate.parse("2025-01-15"));
        verify(attendanceRecordRepository).findByEmployeeIdAndAttendanceDateLessThanEqual(
                "EMP001", LocalDate.parse("2025-01-15"));
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
        record.setAttendanceDate(LocalDate.parse("2025-01-15"));
        record.setClockInTime(LocalTime.parse("09:00"));
        record.setClockOutTime(LocalTime.parse("18:00"));
        record.setStatus("WORK");
        return record;
    }
}
