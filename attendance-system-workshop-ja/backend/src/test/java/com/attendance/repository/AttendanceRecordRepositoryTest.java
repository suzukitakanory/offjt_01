package com.attendance.repository;

import com.attendance.entity.AttendanceRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AttendanceRecordRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Test
    void findByEmployeeIdAndAttendanceDateReturnsMatchingRecord() {
        AttendanceRecord record = attendanceRecordRepository.save(workRecord("EMP001",
                LocalDate.of(2025, 1, 15)));

        var result = attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                "EMP001", LocalDate.of(2025, 1, 15));

        assertTrue(result.isPresent());
        assertEquals(record.getId(), result.orElseThrow().getId());
    }

    @Test
    void findByEmployeeIdAndAttendanceDateDoesNotReturnAnotherDate() {
        attendanceRecordRepository.save(workRecord("EMP001", LocalDate.of(2025, 1, 15)));

        var result = attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                "EMP001", LocalDate.of(2025, 1, 16));

        assertTrue(result.isEmpty());
    }

    private AttendanceRecord workRecord(String employeeId, LocalDate attendanceDate) {
        AttendanceRecord record = new AttendanceRecord();
        record.setEmployeeId(employeeId);
        record.setAttendanceDate(attendanceDate);
        record.setClockInTime(LocalTime.of(9, 0));
        record.setClockOutTime(LocalTime.of(18, 0));
        record.setStatus("WORK");
        return record;
    }
}
