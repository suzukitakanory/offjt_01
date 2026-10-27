package com.attendance.service;

import com.attendance.dto.AttendanceRequest;
import com.attendance.dto.AttendanceResponse;
import com.attendance.entity.AttendanceRecord;
import com.attendance.exception.AttendanceApiException;
import com.attendance.repository.AttendanceRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void saveAttendanceCreatesRecordWithMinutePrecision() {
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                "EMP001", LocalDate.of(2025, 1, 15))).thenReturn(Optional.empty());
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(invocation -> {
                    AttendanceRecord record = invocation.getArgument(0);
                    record.setId(1L);
                    return record;
                });

        AttendanceResponse response = attendanceService.saveAttendance(
                request("EMP001", "2025-01-15", "09:00", "18:00"));

        assertEquals(1L, response.getId());
        assertEquals("09:00:00", response.getClockInTime());
        assertEquals("18:00:00", response.getClockOutTime());
        assertEquals("WORK", response.getStatus());
        verify(attendanceRecordRepository).save(any(AttendanceRecord.class));
    }

    @Test
    void saveAttendanceUpdatesExistingEmployeeDateRecord() {
        AttendanceRecord existing = new AttendanceRecord();
        existing.setId(7L);
        existing.setEmployeeId("EMP001");
        existing.setAttendanceDate(LocalDate.of(2025, 1, 15));
        existing.setClockInTime(LocalTime.of(9, 0));
        existing.setClockOutTime(LocalTime.of(18, 0));
        existing.setStatus("WORK");
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                "EMP001", LocalDate.of(2025, 1, 15))).thenReturn(Optional.of(existing));
        when(attendanceRecordRepository.save(existing)).thenReturn(existing);

        AttendanceResponse response = attendanceService.saveAttendance(
                request("EMP001", "2025-01-15", "08:30", "17:30"));

        assertEquals(7L, response.getId());
        assertEquals("08:30:00", response.getClockInTime());
    }

    @Test
    void getAttendanceByDateReturnsExistingRecord() {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(1L);
        record.setEmployeeId("EMP001");
        record.setAttendanceDate(LocalDate.of(2025, 1, 15));
        record.setClockInTime(LocalTime.of(9, 0));
        record.setClockOutTime(LocalTime.of(18, 0));
        record.setStatus("WORK");
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                "EMP001", LocalDate.of(2025, 1, 15))).thenReturn(Optional.of(record));

        AttendanceResponse response = attendanceService.getAttendanceByDate("EMP001", "2025-01-15")
                .orElseThrow();

        assertEquals("09:00:00", response.getClockInTime());
        assertEquals("18:00:00", response.getClockOutTime());
    }

    @Test
    void saveAttendanceRejectsInvalidTimeFormat() {
        AttendanceApiException exception = assertThrows(AttendanceApiException.class,
                () -> attendanceService.saveAttendance(
                        request("EMP001", "2025-01-15", "25:00", "18:00")));

        assertEquals("ERR03002", exception.getError());
    }

    @Test
    void saveAttendanceRejectsMissingWorkTime() {
        AttendanceApiException exception = assertThrows(AttendanceApiException.class,
                () -> attendanceService.saveAttendance(
                        request("EMP001", "2025-01-15", "09:00", "")));

        assertEquals("ERR03005", exception.getError());
    }

    @Test
    void saveAttendanceRejectsClockInAtOrAfterClockOut() {
        AttendanceApiException exception = assertThrows(AttendanceApiException.class,
                () -> attendanceService.saveAttendance(
                        request("EMP001", "2025-01-15", "18:00", "18:00")));

        assertEquals("ERR03001", exception.getError());
    }

    @Test
    void saveAttendanceRejectsInvalidEmployeeIdAndDate() {
        AttendanceApiException employeeIdException = assertThrows(AttendanceApiException.class,
                () -> attendanceService.saveAttendance(
                        request("employee-1", "2025-01-15", "09:00", "18:00")));
        assertEquals("ERR03003", employeeIdException.getError());

        AttendanceApiException dateException = assertThrows(AttendanceApiException.class,
                () -> attendanceService.saveAttendance(
                        request("EMP001", "2025-02-30", "09:00", "18:00")));
        assertEquals("ERR03004", dateException.getError());
    }

    private AttendanceRequest request(String employeeId, String date, String clockIn, String clockOut) {
        return new AttendanceRequest(employeeId, date, clockIn, clockOut, "WORK", null);
    }
}
