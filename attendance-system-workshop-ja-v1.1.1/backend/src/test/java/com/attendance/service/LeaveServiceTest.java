package com.attendance.service;

import com.attendance.dto.CreateLeaveRequest;
import com.attendance.dto.LeaveResponse;
import com.attendance.entity.AttendanceRecord;
import com.attendance.exception.LeaveApiException;
import com.attendance.repository.AttendanceRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 休暇登録サービスの統合テスト。
 * 関連要件: REQ-UC004 - 休暇登録
 */
@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Testcontainers
class LeaveServiceTest {

    private static final String EMPLOYEE_ID = "EMP001";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Test
    void createsLeaveForUnusedDate() {
        LeaveResponse created = leaveService.create(request("2026-11-09", "Sick Leave", "体調不良", null));

        assertEquals("2026-11-09", created.getDate());
        assertEquals("Sick Leave", created.getLeaveType());
        assertEquals("体調不良", created.getMemo());
        AttendanceRecord stored = find("2026-11-09");
        assertEquals(EMPLOYEE_ID, stored.getEmployeeId());
        assertEquals("SICK_LEAVE", stored.getStatus());
        assertEquals("体調不良", stored.getNotes());
        assertNull(stored.getClockInTime());
        assertNull(stored.getClockOutTime());
    }

    @Test
    void storesEmptyMemoAsNoMemo() {
        LeaveResponse created = leaveService.create(request("2026-11-10", "Personal Leave", "", null));

        assertEquals("", created.getMemo());
        assertNull(find("2026-11-10").getNotes());
        assertEquals("", leaveService.getByDate("2026-11-10").getMemo());
    }

    @Test
    void storesWhitespaceOnlyMemoAsNoMemo() {
        LeaveResponse created = leaveService.create(request("2026-11-11", "Business Trip", " \n\t ", null));

        assertEquals("", created.getMemo());
        assertNull(find("2026-11-11").getNotes());
    }

    @Test
    void rejectsUnsupportedLeaveType() {
        LeaveApiException exception = assertThrows(LeaveApiException.class,
                () -> leaveService.create(request("2026-11-12", "Paid Leave", null, null)));

        assertEquals("ERR03006", exception.getMessageId());
        assertEquals(0, attendanceRecordRepository.findByEmployeeId(EMPLOYEE_ID).stream()
                .filter(record -> record.getAttendanceDate().equals(LocalDate.parse("2026-11-12")))
                .count());
    }

    @Test
    void rejectsMemoLongerThan500Characters() {
        LeaveApiException exception = assertThrows(LeaveApiException.class,
                () -> leaveService.create(request("2026-11-13", "Sick Leave", "あ".repeat(501), null)));

        assertEquals("ERR03007", exception.getMessageId());
    }

    @Test
    void rejectsMissingDate() {
        LeaveApiException exception = assertThrows(LeaveApiException.class,
                () -> leaveService.create(request(" ", "Sick Leave", null, null)));

        assertEquals("ERR03004", exception.getMessageId());
    }

    @Test
    void rejectsMissingLeaveType() {
        LeaveApiException exception = assertThrows(LeaveApiException.class,
                () -> leaveService.create(request("2026-11-14", " ", null, null)));

        assertEquals("ERR03005", exception.getMessageId());
    }

    @Test
    void rejectsSecondLeaveOnSameDateWithoutChangingExistingLeave() {
        leaveService.create(request("2026-11-15", "Sick Leave", "初日", null));

        LeaveApiException exception = assertThrows(LeaveApiException.class,
                () -> leaveService.create(request("2026-11-15", "Personal Leave", "上書き", true)));

        assertEquals("ERR02003", exception.getMessageId());
        AttendanceRecord stored = find("2026-11-15");
        assertEquals("SICK_LEAVE", stored.getStatus());
        assertEquals("初日", stored.getNotes());
    }

    @Test
    void keepsWorkTimesWhenReplacementIsNotConfirmed() {
        saveWork("2026-11-16", LocalTime.of(9, 0), LocalTime.of(18, 0));

        LeaveApiException exception = assertThrows(LeaveApiException.class,
                () -> leaveService.create(request("2026-11-16", "Business Trip", "出張", null)));

        assertEquals("ERR02005", exception.getMessageId());
        AttendanceRecord stored = find("2026-11-16");
        assertEquals("WORK", stored.getStatus());
        assertEquals(LocalTime.of(9, 0), stored.getClockInTime());
        assertEquals(LocalTime.of(18, 0), stored.getClockOutTime());
        assertThrows(LeaveApiException.class, () -> leaveService.getByDate("2026-11-16"));
    }

    @Test
    void replacesConfirmedWorkRowAndClearsClockTimes() {
        AttendanceRecord work = saveWork("2026-11-17", LocalTime.of(8, 30), LocalTime.of(17, 30));

        LeaveResponse replaced = leaveService.create(request("2026-11-17", "Sick Leave", "体調不良", true));

        assertEquals("Sick Leave", replaced.getLeaveType());
        assertEquals("体調不良", replaced.getMemo());
        AttendanceRecord stored = find("2026-11-17");
        assertEquals(work.getId(), stored.getId());
        assertEquals("SICK_LEAVE", stored.getStatus());
        assertNull(stored.getClockInTime());
        assertNull(stored.getClockOutTime());
        assertEquals("Sick Leave", leaveService.getByDate("2026-11-17").getLeaveType());
    }

    private CreateLeaveRequest request(String date, String leaveType, String memo, Boolean confirmReplaceWork) {
        return new CreateLeaveRequest(date, leaveType, memo, confirmReplaceWork);
    }

    private AttendanceRecord saveWork(String date, LocalTime clockIn, LocalTime clockOut) {
        AttendanceRecord record = new AttendanceRecord();
        record.setEmployeeId(EMPLOYEE_ID);
        record.setAttendanceDate(LocalDate.parse(date));
        record.setStatus("WORK");
        record.setClockInTime(clockIn);
        record.setClockOutTime(clockOut);
        return attendanceRecordRepository.saveAndFlush(record);
    }

    private AttendanceRecord find(String date) {
        return attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                EMPLOYEE_ID, LocalDate.parse(date)).orElseThrow();
    }
}
