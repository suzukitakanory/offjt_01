package com.attendance.repository;

import com.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 勤怠レコードリポジトリ
 * 関連要件: REQ-UC002 - カレンダー操作, REQ-UC003 - 勤務時間入力, REQ-UC004 - 休暇登録
 */
@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    // TODO: REQ-UC002 - 従業員IDでの勤怠レコード一覧取得
    List<AttendanceRecord> findByEmployeeId(String employeeId);

    // TODO: REQ-UC003 - 従業員IDと日付での勤怠レコード取得
    Optional<AttendanceRecord> findByEmployeeIdAndAttendanceDate(String employeeId, LocalDate attendanceDate);
}
