package com.attendance.repository;

import com.attendance.entity.AttendanceRecord;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 勤怠レコードリポジトリテスト
 * 関連要件: REQ-UC002 - カレンダー操作, REQ-UC003 - 勤務時間入力, REQ-UC004 - 休暇登録
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AttendanceRecordRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withCopyFileToContainer(MountableFile.forHostPath("init-db/01-schema.sql"),
                    "/docker-entrypoint-initdb.d/01-schema.sql");

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findsRecordsByEmployeeId() {
        attendanceRecordRepository.saveAll(List.of(
                record("EMP001", "2025-01-15", "WORK", LocalTime.of(9, 0), LocalTime.of(18, 0)),
                record("EMP002", "2025-01-15", "WORK", LocalTime.of(9, 0), LocalTime.of(18, 0))));

        assertEquals(1, attendanceRecordRepository.findByEmployeeId("EMP001").size());
    }

    @Test
    void findsRecordByEmployeeAndDate() {
        attendanceRecordRepository.save(record("EMP001", "2025-01-15", "WORK",
                LocalTime.of(9, 0), LocalTime.of(18, 0)));

        assertEquals("EMP001", attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                "EMP001", LocalDate.parse("2025-01-15")).orElseThrow().getEmployeeId());
    }

    @Test
    void findsRecordsAtBothInclusiveRangeBoundaries() {
        attendanceRecordRepository.saveAll(List.of(
                record("EMP001", "2025-01-01", "WORK", LocalTime.of(9, 0), LocalTime.of(18, 0)),
                record("EMP001", "2025-01-15", "WORK", LocalTime.of(9, 0), LocalTime.of(18, 0)),
                record("EMP001", "2025-01-31", "WORK", LocalTime.of(9, 0), LocalTime.of(18, 0)),
                record("EMP001", "2025-02-01", "WORK", LocalTime.of(9, 0), LocalTime.of(18, 0))));

        assertEquals(3, attendanceRecordRepository.findByEmployeeIdAndAttendanceDateBetween(
                "EMP001", LocalDate.parse("2025-01-01"), LocalDate.parse("2025-01-31")).size());
        assertEquals(2, attendanceRecordRepository.findByEmployeeIdAndAttendanceDateGreaterThanEqual(
                "EMP001", LocalDate.parse("2025-01-31")).size());
        assertEquals(2, attendanceRecordRepository.findByEmployeeIdAndAttendanceDateLessThanEqual(
                "EMP001", LocalDate.parse("2025-01-15")).size());
        assertEquals(0, attendanceRecordRepository.findByEmployeeIdAndAttendanceDateBetween(
                "EMP001", LocalDate.parse("2025-01-31"), LocalDate.parse("2025-01-01")).size());
    }

    @Test
    void rejectsDuplicateEmployeeAndDate() {
        attendanceRecordRepository.saveAndFlush(record("EMP001", "2025-01-15", "WORK",
                LocalTime.of(9, 0), LocalTime.of(18, 0)));

        assertThrows(DataIntegrityViolationException.class, () -> attendanceRecordRepository.saveAndFlush(
                record("EMP001", "2025-01-15", "WORK", LocalTime.of(8, 0), LocalTime.of(17, 0))));
    }

    @Test
    void rejectsEqualClockTimes() {
        assertThrows(DataIntegrityViolationException.class, () -> attendanceRecordRepository.saveAndFlush(
                record("EMP001", "2025-01-15", "WORK", LocalTime.of(9, 0), LocalTime.of(9, 0))));
    }

    @Test
    void permitsLeaveWithoutClockTimes() {
        AttendanceRecord saved = attendanceRecordRepository.saveAndFlush(
                record("EMP001", "2025-01-15", "PAID_LEAVE", null, null));

        assertEquals("PAID_LEAVE", saved.getStatus());
    }

    @Test
    void updatesTimestampWithoutChangingCreationTimestamp() {
        AttendanceRecord saved = attendanceRecordRepository.saveAndFlush(record(
                "EMP001", "2025-01-15", "WORK", LocalTime.of(9, 0), LocalTime.of(18, 0)));
        LocalDateTime createdAt = LocalDateTime.parse("2020-01-01T00:00:00");
        jdbcTemplate.update("UPDATE attendance_records SET created_at = ?, updated_at = ? WHERE id = ?",
                createdAt, createdAt, saved.getId());
        entityManager.clear();

        AttendanceRecord existing = attendanceRecordRepository.findById(saved.getId()).orElseThrow();
        existing.setClockInTime(LocalTime.of(8, 30));
        attendanceRecordRepository.flush();
        entityManager.clear();

        AttendanceRecord updated = attendanceRecordRepository.findById(saved.getId()).orElseThrow();
        assertEquals(createdAt, updated.getCreatedAt());
        org.junit.jupiter.api.Assertions.assertTrue(updated.getUpdatedAt().isAfter(createdAt));
    }

    private AttendanceRecord record(String employeeId, String date, String status,
                                    LocalTime clockIn, LocalTime clockOut) {
        AttendanceRecord record = new AttendanceRecord();
        record.setEmployeeId(employeeId);
        record.setAttendanceDate(LocalDate.parse(date));
        record.setStatus(status);
        record.setClockInTime(clockIn);
        record.setClockOutTime(clockOut);
        return record;
    }
}
