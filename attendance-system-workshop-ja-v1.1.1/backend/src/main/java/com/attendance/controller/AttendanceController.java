package com.attendance.controller;

import com.attendance.dto.AttendanceRequest;
import com.attendance.dto.AttendanceResponse;
import com.attendance.exception.AttendanceApiException;
import com.attendance.service.AttendanceService;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<AttendanceResponse> saveAttendance(@RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.saveAttendance(request));
    }

    @GetMapping("/{employeeId}/{date}")
    public ResponseEntity<AttendanceResponse> getAttendanceByDate(
            @PathVariable String employeeId,
            @PathVariable String date) {
        AttendanceResponse response = attendanceService.getAttendanceByDate(employeeId, date)
                .orElseThrow(() -> new AttendanceApiException(
                        HttpStatus.NOT_FOUND, "ERR02001", "指定した日付の勤怠レコードはありません"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendancesByEmployeeId(
            @PathVariable String employeeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(
                attendanceService.getAttendancesByEmployeeId(employeeId, startDate, endDate));
    }
}
