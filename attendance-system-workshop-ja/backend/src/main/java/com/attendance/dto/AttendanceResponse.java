package com.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 勤怠レコードレスポンスDTO
 * 関連要件: REQ-UC002 - カレンダー操作, REQ-UC003 - 勤務時間入力, REQ-UC004 - 休暇登録
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    // TODO: REQ-UC002, REQ-UC003, REQ-UC004 - レスポンスフィールドの詳細定義
    private Long id;
    private String employeeId;
    private String attendanceDate;
    private String clockInTime;
    private String clockOutTime;
    private String status;
    private String notes;
    private String createdAt;
    private String updatedAt;
}
