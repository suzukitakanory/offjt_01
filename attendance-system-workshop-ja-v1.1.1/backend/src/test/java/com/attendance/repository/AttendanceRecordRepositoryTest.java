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

import static org.junit.jupiter.api.Assertions.assertTrue;

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
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Test
    void testFindByEmployeeId() {
        // TODO: REQ-UC002 - 従業員IDでの勤怠レコード一覧取得テスト
        assertTrue(true);
    }

    @Test
    void testFindByEmployeeIdAndAttendanceDate() {
        // TODO: REQ-UC003 - 従業員IDと日付での勤怠レコード取得テスト
        assertTrue(true);
    }
}
