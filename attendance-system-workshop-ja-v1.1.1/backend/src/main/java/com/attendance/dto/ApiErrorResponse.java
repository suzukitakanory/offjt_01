package com.attendance.dto;

import java.util.Map;

/**
 * 休暇 API のエラー応答。error はメッセージ ID、message は利用者向け文言だけを返す。
 */
public record ApiErrorResponse(String error, String message) {

    public static final String ERR02002 = "ERR02002";
    public static final String ERR02003 = "ERR02003";
    public static final String ERR02005 = "ERR02005";
    public static final String ERR03004 = "ERR03004";
    public static final String ERR03005 = "ERR03005";
    public static final String ERR03006 = "ERR03006";
    public static final String ERR03007 = "ERR03007";
    public static final String ERR03008 = "ERR03008";

    private static final Map<String, String> MESSAGES = Map.of(
            ERR02002, "指定した日付の休暇はありません",
            ERR02003, "この日付にはすでに休暇が登録されています。変更または削除を使ってください",
            ERR02005, "この日付には勤務時間が登録されています。休暇にすると出勤・退勤時刻は削除されます",
            ERR03004, "休暇日を指定してください",
            ERR03005, "休暇区分を選択してください",
            ERR03006, "対応する休暇区分を選択してください",
            ERR03007, "メモは500文字以内で入力してください",
            ERR03008, "開始日と終了日は両方指定してください");

    public ApiErrorResponse {
        String userMessage = MESSAGES.get(error);
        if (userMessage == null || !userMessage.equals(message)) {
            throw new IllegalArgumentException("unknown leave message id");
        }
    }

    public static ApiErrorResponse of(String messageId) {
        String userMessage = MESSAGES.get(messageId);
        if (userMessage == null) {
            throw new IllegalArgumentException("unknown leave message id");
        }
        return new ApiErrorResponse(messageId, userMessage);
    }
}
