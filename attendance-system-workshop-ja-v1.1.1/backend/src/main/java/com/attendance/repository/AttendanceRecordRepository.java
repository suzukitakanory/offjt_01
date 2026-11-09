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

    /** 従業員の勤怠レコードをすべて取得する。 */
    List<AttendanceRecord> findByEmployeeId(String employeeId);

    /** 従業員と勤怠日を指定してレコードを取得する。 */
    Optional<AttendanceRecord> findByEmployeeIdAndAttendanceDate(String employeeId, LocalDate attendanceDate);

    /** 従業員と開始日・終了日（両端を含む）を指定して取得する。 */
    List<AttendanceRecord> findByEmployeeIdAndAttendanceDateBetween(
            String employeeId, LocalDate startDate, LocalDate endDate);

    /** 指定した開始日以降の勤怠レコードを取得する。 */
    List<AttendanceRecord> findByEmployeeIdAndAttendanceDateGreaterThanEqual(String employeeId, LocalDate startDate);

    /** 指定した終了日以前の勤怠レコードを取得する。 */
    List<AttendanceRecord> findByEmployeeIdAndAttendanceDateLessThanEqual(String employeeId, LocalDate endDate);
}
