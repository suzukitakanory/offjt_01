package com.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 勤怠レコードリクエストDTO
 * 関連要件: REQ-UC003 - 勤務時間入力, REQ-UC004 - 休暇登録
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequest {

    // TODO: REQ-UC003, REQ-UC004 - リクエストフィールドの詳細定義
    private String employeeId;
    private String attendanceDate;
    private String clockInTime;
    private String clockOutTime;
    private String status;
    private String notes;
}
