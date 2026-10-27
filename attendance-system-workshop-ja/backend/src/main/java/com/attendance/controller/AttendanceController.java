package com.attendance.controller;

import com.attendance.dto.AttendanceRequest;
import com.attendance.dto.AttendanceResponse;
import com.attendance.exception.AttendanceApiException;
import com.attendance.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
     */
    @PostMapping
    public ResponseEntity<AttendanceResponse> saveAttendance(@RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.saveAttendance(request));
    }

    /**
     * 特定日の勤怠レコードを取得する
     */
    @GetMapping("/{employeeId}/{date}")
    public ResponseEntity<AttendanceResponse> getAttendanceByDate(
            @PathVariable("employeeId") String employeeId,
            @PathVariable("date") String date) {
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(employeeId, date)
                .orElseThrow(() -> new AttendanceApiException(
                        HttpStatus.NOT_FOUND,
                        "Attendance record not found",
                        "指定された日付の勤怠レコードが見つかりません",
                        Map.of())));
    }

    /**
     * 特定日の休暇レコードを削除する
     */
    @DeleteMapping("/{employeeId}/{date}")
    public ResponseEntity<Void> deleteLeaveByDate(
            @PathVariable("employeeId") String employeeId,
            @PathVariable("date") String date) {
        attendanceService.deleteLeaveByDate(employeeId, date);
        return ResponseEntity.noContent().build();
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
