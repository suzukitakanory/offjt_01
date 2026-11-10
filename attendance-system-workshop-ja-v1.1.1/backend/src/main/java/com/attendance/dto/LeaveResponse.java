package com.attendance.dto;

import com.attendance.entity.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 休暇の応答。休暇区分は表示名、メモなしは空文字にする。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveResponse {

    private String date;
    private String leaveType;
    private String memo;

    public static LeaveResponse of(String date, LeaveType leaveType, String notes) {
        return new LeaveResponse(date, leaveType.getDisplayName(), notes == null ? "" : notes);
    }

    public String getMemo() {
        return memo == null ? "" : memo;
    }
}
