package com.attendance.dto;

import com.attendance.entity.LeaveType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 休暇の更新要求。日付と従業員 ID は受け取らない。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateLeaveRequest {

    @NotBlank(message = "ERR03005")
    @LeaveType.Supported
    private String leaveType;

    @Size(max = 500, message = "ERR03007")
    private String memo;
}
