package com.attendance.exception;

import com.attendance.dto.ApiErrorResponse;

/**
 * 休暇 API の業務エラー。応答へ載せるのはメッセージ ID だけで、原因例外の詳細は持たない。
 */
public class LeaveApiException extends RuntimeException {

    private final String messageId;

    private LeaveApiException(String messageId) {
        super(messageId);
        this.messageId = messageId;
    }

    public static LeaveApiException missingDate() {
        return new LeaveApiException(ApiErrorResponse.ERR03004);
    }

    public static LeaveApiException missingLeaveType() {
        return new LeaveApiException(ApiErrorResponse.ERR03005);
    }

    public static LeaveApiException unsupportedLeaveType() {
        return new LeaveApiException(ApiErrorResponse.ERR03006);
    }

    public static LeaveApiException memoTooLong() {
        return new LeaveApiException(ApiErrorResponse.ERR03007);
    }

    public static LeaveApiException incompletePeriod() {
        return new LeaveApiException(ApiErrorResponse.ERR03008);
    }

    public static LeaveApiException leaveNotFound() {
        return new LeaveApiException(ApiErrorResponse.ERR02002);
    }

    public static LeaveApiException duplicateLeave() {
        return new LeaveApiException(ApiErrorResponse.ERR02003);
    }

    public static LeaveApiException replaceRequiresConfirmation() {
        return new LeaveApiException(ApiErrorResponse.ERR02005);
    }

    public String getMessageId() {
        return messageId;
    }
}
