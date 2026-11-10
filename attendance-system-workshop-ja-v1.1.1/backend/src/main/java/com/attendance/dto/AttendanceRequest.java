package com.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank
    @Pattern(regexp = "EMP[0-9]{3}")
    private String employeeId;

    @NotBlank
    @Pattern(regexp = "[0-9]{4}-[0-9]{2}-[0-9]{2}")
    private String attendanceDate;

    @NotBlank
    @Pattern(regexp = "([01][0-9]|2[0-3]):[0-5][0-9]")
    private String clockInTime;

    @NotBlank
    @Pattern(regexp = "([01][0-9]|2[0-3]):[0-5][0-9]")
    private String clockOutTime;

    @NotBlank
    @Pattern(regexp = "WORK|PAID_LEAVE|SICK_LEAVE|UNPAID_LEAVE")
    private String status;

    private String notes;
}
