package com.attendance.service;

import com.attendance.dto.CreateLeaveRequest;
import com.attendance.dto.LeaveResponse;
import com.attendance.dto.UpdateLeaveRequest;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    /** 未使用日に登録し、返却値とDB上の区分・メモ・時刻を確認する。 */
    @Test
    void createsLeaveForUnusedDate() {
        LeaveResponse created = leaveService.create(request("2026-11-09", "Sick Leave", "体調不良", null))
                .response();

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

    /** 空文字のメモをNULLで保存し、取得時は空文字で返すことを確認する。 */
    @Test
    void storesEmptyMemoAsNoMemo() {
        LeaveResponse created = leaveService.create(request("2026-11-10", "Personal Leave", "", null))
                .response();

        assertEquals("", created.getMemo());
        assertNull(find("2026-11-10").getNotes());
        assertEquals("", leaveService.getByDate("2026-11-10").getMemo());
    }

    /** 空白だけのメモをNULLで保存することを確認する。 */
    @Test
    void storesWhitespaceOnlyMemoAsNoMemo() {
        LeaveResponse created = leaveService.create(request("2026-11-11", "Business Trip", " \n\t ", null))
                .response();

        assertEquals("", created.getMemo());
        assertNull(find("2026-11-11").getNotes());
    }

    /** 未対応区分を拒否し、勤怠レコードを作成しないことを確認する。 */
    @Test
    void rejectsUnsupportedLeaveType() {
        LeaveApiException exception = assertThrows(LeaveApiException.class,
                () -> leaveService.create(request("2026-11-12", "Paid Leave", null, null)));

        assertEquals("ERR03006", exception.getMessageId());
        assertEquals(0, attendanceRecordRepository.findByEmployeeId(EMPLOYEE_ID).stream()
                .filter(record -> record.getAttendanceDate().equals(LocalDate.parse("2026-11-12")))
                .count());
    }

    /** 501文字のメモをERR03007で拒否することを確認する。 */
    @Test
    void rejectsMemoLongerThan500Characters() {
        LeaveApiException exception = assertThrows(LeaveApiException.class,
                () -> leaveService.create(request("2026-11-13", "Sick Leave", "あ".repeat(501), null)));

        assertEquals("ERR03007", exception.getMessageId());
    }

    /** 日付が未指定の場合にERR03004で拒否することを確認する。 */
    @Test
    void rejectsMissingDate() {
        LeaveApiException exception = assertThrows(LeaveApiException.class,
                () -> leaveService.create(request(" ", "Sick Leave", null, null)));

        assertEquals("ERR03004", exception.getMessageId());
    }

    /** 休暇区分が未指定の場合にERR03005で拒否することを確認する。 */
    @Test
    void rejectsMissingLeaveType() {
        LeaveApiException exception = assertThrows(LeaveApiException.class,
                () -> leaveService.create(request("2026-11-14", " ", null, null)));

        assertEquals("ERR03005", exception.getMessageId());
    }

    /** 同日に休暇がある場合の重複拒否と、既存区分・メモが不変であることを確認する。 */
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

    /** 未確認の勤務行を拒否し、既存の勤務区分と出退勤時刻を保持することを確認する。 */
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

    /** 確認済み勤務行を同じレコードのまま休暇に置換し、出退勤時刻を消すことを確認する。 */
    @Test
    void replacesConfirmedWorkRowAndClearsClockTimes() {
        AttendanceRecord work = saveWork("2026-11-17", LocalTime.of(8, 30), LocalTime.of(17, 30));

        LeaveService.CreationResult result =
                leaveService.create(request("2026-11-17", "Sick Leave", "体調不良", true));
        LeaveResponse replaced = result.response();

        assertTrue(result.replacedWork());
        assertEquals("Sick Leave", replaced.getLeaveType());
        assertEquals("体調不良", replaced.getMemo());
        AttendanceRecord stored = find("2026-11-17");
        assertEquals(work.getId(), stored.getId());
        assertEquals("SICK_LEAVE", stored.getStatus());
        assertNull(stored.getClockInTime());
        assertNull(stored.getClockOutTime());
        assertEquals("Sick Leave", leaveService.getByDate("2026-11-17").getLeaveType());
    }

    /** 区分とメモを更新し、日付と出退勤時刻が維持されることを検証する。 */
    @Test
    void updatesLeaveTypeAndMemoWithoutChangingDateOrClockTimes() {
        AttendanceRecord leave = new AttendanceRecord();
        leave.setEmployeeId(EMPLOYEE_ID);
        leave.setAttendanceDate(LocalDate.parse("2026-11-18"));
        leave.setStatus("SICK_LEAVE");
        leave.setNotes("更新前");
        leave.setClockInTime(LocalTime.of(9, 0));
        leave.setClockOutTime(LocalTime.of(18, 0));
        attendanceRecordRepository.saveAndFlush(leave);

        LeaveResponse updated = leaveService.update(
                "2026-11-18", new UpdateLeaveRequest("Personal Leave", "更新後"));

        assertEquals("2026-11-18", updated.getDate());
        assertEquals("Personal Leave", updated.getLeaveType());
        assertEquals("更新後", updated.getMemo());
        AttendanceRecord stored = find("2026-11-18");
        assertEquals(leave.getId(), stored.getId());
        assertEquals(LocalDate.parse("2026-11-18"), stored.getAttendanceDate());
        assertEquals("PERSONAL_LEAVE", stored.getStatus());
        assertEquals("更新後", stored.getNotes());
        assertEquals(LocalTime.of(9, 0), stored.getClockInTime());
        assertEquals(LocalTime.of(18, 0), stored.getClockOutTime());
    }

    /** 更新時に空白のみのメモが空として保存されることを検証する。 */
    @Test
    void storesWhitespaceOnlyMemoAsNoMemoWhenUpdating() {
        leaveService.create(request("2026-11-19", "Sick Leave", "更新前", null));

        LeaveResponse updated = leaveService.update(
                "2026-11-19", new UpdateLeaveRequest("Business Trip", " \n\t "));

        assertEquals("", updated.getMemo());
        assertNull(find("2026-11-19").getNotes());
    }

    /** 休暇が存在しない日付を更新せず、レコードも作成しないことを検証する。 */
    @Test
    void rejectsUpdateWhenLeaveDoesNotExist() {
        LeaveApiException exception = assertThrows(LeaveApiException.class,
                () -> leaveService.update("2026-11-20", new UpdateLeaveRequest("Sick Leave", "更新")));

        assertEquals("ERR02002", exception.getMessageId());
        assertEquals(0, attendanceRecordRepository.findByEmployeeId(EMPLOYEE_ID).stream()
                .filter(record -> record.getAttendanceDate().equals(LocalDate.parse("2026-11-20")))
                .count());
    }

    /** 勤務のみの日を更新対象にせず、勤務状態と時刻を維持することを検証する。 */
    @Test
    void rejectsUpdateWhenDateHasOnlyWorkAndKeepsWorkTimes() {
        saveWork("2026-11-21", LocalTime.of(9, 0), LocalTime.of(18, 0));

        LeaveApiException exception = assertThrows(LeaveApiException.class,
                () -> leaveService.update("2026-11-21", new UpdateLeaveRequest("Sick Leave", "更新")));

        assertEquals("ERR02002", exception.getMessageId());
        AttendanceRecord stored = find("2026-11-21");
        assertEquals("WORK", stored.getStatus());
        assertEquals(LocalTime.of(9, 0), stored.getClockInTime());
        assertEquals(LocalTime.of(18, 0), stored.getClockOutTime());
    }

    private CreateLeaveRequest request(String date, String leaveType, String memo, Boolean confirmReplaceWork) {
        return new CreateLeaveRequest(date, leaveType, memo, confirmReplaceWork);
    }

    /** 勤務行をDBに用意し、勤務日の置換動作を検証できるようにする。 */
    private AttendanceRecord saveWork(String date, LocalTime clockIn, LocalTime clockOut) {
        AttendanceRecord record = new AttendanceRecord();
        record.setEmployeeId(EMPLOYEE_ID);
        record.setAttendanceDate(LocalDate.parse(date));
        record.setStatus("WORK");
        record.setClockInTime(clockIn);
        record.setClockOutTime(clockOut);
        return attendanceRecordRepository.saveAndFlush(record);
    }

    /** 指定日のDBレコードを取得し、保存結果の検証に使う。 */
    private AttendanceRecord find(String date) {
        return attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                EMPLOYEE_ID, LocalDate.parse(date)).orElseThrow();
    }
}
