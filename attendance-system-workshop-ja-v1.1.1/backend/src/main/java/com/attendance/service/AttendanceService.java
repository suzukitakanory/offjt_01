package com.attendance.service;

import com.attendance.dto.AttendanceRequest;
import com.attendance.dto.AttendanceResponse;
import com.attendance.entity.AttendanceRecord;
import com.attendance.repository.AttendanceRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 勤怠サービス
 * 関連要件: REQ-UC002 - カレンダー操作, REQ-UC003 - 勤務時間入力, REQ-UC004 - 休暇登録
 */
@Service
@Transactional
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;

    public AttendanceService(AttendanceRecordRepository attendanceRecordRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    /**
     * 勤怠レコードを保存する
     * TODO: REQ-UC003, REQ-UC004 - 勤怠レコード保存ロジックの実装
     */
    public AttendanceResponse saveAttendance(AttendanceRequest request) {
        // TODO: REQ-UC003, REQ-UC004 - 実装予定
        return null;
    }

    /**
     * 従業員IDと日付で勤怠レコードを取得する
     * TODO: REQ-UC003 - 勤怠レコード取得ロジックの実装
     */
    public Optional<AttendanceResponse> getAttendanceByDate(String employeeId, String date) {
        // TODO: REQ-UC003 - 実装予定
        return Optional.empty();
    }

    /**
     * 従業員IDで勤怠レコード一覧を取得する
     * TODO: REQ-UC002 - 勤怠レコード一覧取得ロジックの実装
     */
    public List<AttendanceResponse> getAttendancesByEmployeeId(String employeeId) {
        // TODO: REQ-UC002 - 実装予定
        return List.of();
    }
}
