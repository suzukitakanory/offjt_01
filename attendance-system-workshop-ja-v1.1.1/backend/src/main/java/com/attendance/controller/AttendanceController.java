package com.attendance.controller;

import com.attendance.dto.AttendanceRequest;
import com.attendance.dto.AttendanceResponse;
import com.attendance.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 勤怠コントローラー
 * 関連要件: REQ-UC002 - カレンダー操作, REQ-UC003 - 勤務時間入力, REQ-UC004 - 休暇登録
 */
@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "http://localhost:5173")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * 勤怠レコードを登録・更新する
     * TODO: REQ-UC003, REQ-UC004 - 勤怠レコード登録・更新APIの実装
     */
    @PostMapping
    public ResponseEntity<AttendanceResponse> saveAttendance(@RequestBody AttendanceRequest request) {
        // TODO: REQ-UC003, REQ-UC004 - 実装予定
        return ResponseEntity.ok(null);
    }

    /**
     * 特定日の勤怠レコードを取得する
     * TODO: REQ-UC003 - 特定日勤怠レコード取得APIの実装
     */
    @GetMapping("/{employeeId}/{date}")
    public ResponseEntity<AttendanceResponse> getAttendanceByDate(
            @PathVariable String employeeId,
            @PathVariable String date) {
        // TODO: REQ-UC003 - 実装予定
        return ResponseEntity.ok(null);
    }

    /**
     * 従業員の勤怠レコード一覧を取得する
     * TODO: REQ-UC002 - 勤怠レコード一覧取得APIの実装
     */
    @GetMapping("/{employeeId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendancesByEmployeeId(
            @PathVariable String employeeId) {
        // TODO: REQ-UC002 - 実装予定
        return ResponseEntity.ok(List.of());
    }
}
