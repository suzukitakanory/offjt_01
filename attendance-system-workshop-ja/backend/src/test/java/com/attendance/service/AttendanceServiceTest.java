package com.attendance.service;

import com.attendance.dto.AttendanceRequest;
import com.attendance.dto.AttendanceResponse;
import com.attendance.entity.AttendanceRecord;
import com.attendance.exception.AttendanceApiException;
import com.attendance.repository.AttendanceRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @ParameterizedTest
    @ValueSource(strings = {"SICK_LEAVE", "PERSONAL_LEAVE", "BUSINESS_TRIP"})
    void saveAttendanceCreatesUc004LeaveWithNullTimes(String status) {
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                "EMP001", LocalDate.of(2025, 1, 15))).thenReturn(Optional.empty());
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(invocation -> {
                    AttendanceRecord record = invocation.getArgument(0);
                    record.setId(2L);
                    return record;
                });

        AttendanceResponse response = attendanceService.saveAttendance(
                leaveRequest("2025-01-15", status, "休暇メモ"));

        assertEquals(2L, response.getId());
        assertEquals(status, response.getStatus());
        assertEquals("休暇メモ", response.getNotes());
        assertNull(response.getClockInTime());
        assertNull(response.getClockOutTime());
        verify(attendanceRecordRepository).save(any(AttendanceRecord.class));
    }

    @Test
    void saveAttendanceAllowsFutureLeaveWithoutNotes() {
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                "EMP001", LocalDate.of(2099, 1, 15))).thenReturn(Optional.empty());
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.saveAttendance(
                leaveRequest("2099-01-15", "PERSONAL_LEAVE", null));

        assertEquals("2099-01-15", response.getAttendanceDate());
        assertEquals("PERSONAL_LEAVE", response.getStatus());
        assertNull(response.getNotes());
        assertNull(response.getClockInTime());
        assertNull(response.getClockOutTime());
    }

    @Test
    void saveAttendanceUpdatesExistingUc004LeaveRecord() {
        AttendanceRecord existing = leaveRecord("SICK_LEAVE");
        existing.setId(7L);
        existing.setNotes("以前のメモ");
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                "EMP001", LocalDate.of(2025, 1, 15))).thenReturn(Optional.of(existing));
        when(attendanceRecordRepository.save(existing)).thenReturn(existing);

        AttendanceResponse response = attendanceService.saveAttendance(
                leaveRequest("2025-01-15", "BUSINESS_TRIP", "出張に変更"));

        assertEquals(7L, response.getId());
        assertEquals("BUSINESS_TRIP", response.getStatus());
        assertEquals("出張に変更", response.getNotes());
        assertNull(response.getClockInTime());
        assertNull(response.getClockOutTime());
        verify(attendanceRecordRepository).save(existing);
    }

    @ParameterizedTest
    @ValueSource(strings = {"WORK", "PAID_LEAVE", "UNPAID_LEAVE"})
    void saveAttendanceRejectsUc004LeaveWhenExistingRecordIsOutOfScope(String status) {
        AttendanceRecord existing = new AttendanceRecord();
        existing.setId(9L);
        existing.setEmployeeId("EMP001");
        existing.setAttendanceDate(LocalDate.of(2025, 1, 15));
        existing.setStatus(status);
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                "EMP001", LocalDate.of(2025, 1, 15))).thenReturn(Optional.of(existing));

        AttendanceApiException exception = assertThrows(AttendanceApiException.class,
                () -> attendanceService.saveAttendance(
                        leaveRequest("2025-01-15", "SICK_LEAVE", null)));

        assertEquals("ERR02001", exception.getError());
        assertEquals(org.springframework.http.HttpStatus.CONFLICT, exception.getStatus());
        verify(attendanceRecordRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    void saveAttendanceRejectsClockTimesForLeave() {
        AttendanceRequest request =
                new AttendanceRequest("EMP001", "2025-01-15", "09:00", null, "SICK_LEAVE", null);

        AttendanceApiException exception = assertThrows(AttendanceApiException.class,
                () -> attendanceService.saveAttendance(request));

        assertEquals("ERR03007", exception.getError());
        verify(attendanceRecordRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    void saveAttendanceRejectsUnknownLeaveStatus() {
        AttendanceRequest request =
                new AttendanceRequest("EMP001", "2025-01-15", null, null, "ANNUAL_LEAVE", null);

        AttendanceApiException exception = assertThrows(AttendanceApiException.class,
                () -> attendanceService.saveAttendance(request));

        assertEquals("ERR03006", exception.getError());
        verify(attendanceRecordRepository, never()).save(any(AttendanceRecord.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SICK_LEAVE", "PERSONAL_LEAVE", "BUSINESS_TRIP"})
    void deleteLeaveByDateDeletesUc004LeaveRecord(String status) {
        AttendanceRecord existing = leaveRecord(status);
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                "EMP001", LocalDate.of(2025, 1, 15))).thenReturn(Optional.of(existing));

        attendanceService.deleteLeaveByDate("EMP001", "2025-01-15");

        verify(attendanceRecordRepository).delete(existing);
    }

    @Test
    void deleteLeaveByDateReturnsNotFoundWhenRecordDoesNotExist() {
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                "EMP001", LocalDate.of(2025, 1, 15))).thenReturn(Optional.empty());

        AttendanceApiException exception = assertThrows(AttendanceApiException.class,
                () -> attendanceService.deleteLeaveByDate("EMP001", "2025-01-15"));

        assertEquals("ERR02002", exception.getError());
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, exception.getStatus());
        verify(attendanceRecordRepository, never()).delete(any(AttendanceRecord.class));
    }

    @Test
    void deleteLeaveByDateRejectsOutOfScopeAttendanceRecord() {
        AttendanceRecord existing = new AttendanceRecord();
        existing.setEmployeeId("EMP001");
        existing.setAttendanceDate(LocalDate.of(2025, 1, 15));
        existing.setStatus("WORK");
        when(attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                "EMP001", LocalDate.of(2025, 1, 15))).thenReturn(Optional.of(existing));

        AttendanceApiException exception = assertThrows(AttendanceApiException.class,
                () -> attendanceService.deleteLeaveByDate("EMP001", "2025-01-15"));

        assertEquals("ERR02001", exception.getError());
        assertEquals(org.springframework.http.HttpStatus.CONFLICT, exception.getStatus());
        verify(attendanceRecordRepository, never()).delete(any(AttendanceRecord.class));
    }

    @Test
    void deleteLeaveByDateRejectsInvalidEmployeeIdAndDate() {
        AttendanceApiException employeeIdException = assertThrows(AttendanceApiException.class,
                () -> attendanceService.deleteLeaveByDate("employee-1", "2025-01-15"));
        assertEquals("ERR03003", employeeIdException.getError());

        AttendanceApiException dateException = assertThrows(AttendanceApiException.class,
                () -> attendanceService.deleteLeaveByDate("EMP001", "2025-02-30"));
        assertEquals("ERR03004", dateException.getError());

        verify(attendanceRecordRepository, never())
                .findByEmployeeIdAndAttendanceDate(any(), any());
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

    private AttendanceRequest leaveRequest(String date, String status, String notes) {
        return new AttendanceRequest("EMP001", date, null, null, status, notes);
    }

    private AttendanceRecord leaveRecord(String status) {
        AttendanceRecord record = new AttendanceRecord();
        record.setEmployeeId("EMP001");
        record.setAttendanceDate(LocalDate.of(2025, 1, 15));
        record.setStatus(status);
        record.setNotes("休暇メモ");
        return record;
    }
}
