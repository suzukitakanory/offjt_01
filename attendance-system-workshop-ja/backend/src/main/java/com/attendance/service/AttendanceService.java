package com.attendance.service;

import com.attendance.dto.AttendanceRequest;
import com.attendance.dto.AttendanceResponse;
import com.attendance.entity.AttendanceRecord;
import com.attendance.exception.AttendanceApiException;
import com.attendance.repository.AttendanceRecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 勤怠サービス
 * 関連要件: REQ-UC002 - カレンダー操作, REQ-UC003 - 勤務時間入力, REQ-UC004 - 休暇登録
 */
@Service
@Transactional
public class AttendanceService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final Set<String> ALLOWED_STATUSES =
            Set.of("WORK", "PAID_LEAVE", "SICK_LEAVE", "UNPAID_LEAVE");

    private final AttendanceRecordRepository attendanceRecordRepository;

    public AttendanceService(AttendanceRecordRepository attendanceRecordRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    /**
     * 勤怠レコードを保存する
     */
    public AttendanceResponse saveAttendance(AttendanceRequest request) {
        String employeeId = validateEmployeeId(request.getEmployeeId());
        LocalDate attendanceDate = parseAttendanceDate(request.getAttendanceDate());
        String status = request.getStatus();
        if (status == null || !ALLOWED_STATUSES.contains(status)) {
            throw validationError("ERR03006", "勤怠ステータスが不正です", "status",
                    status, "定義された勤怠ステータスを指定してください");
        }

        LocalTime clockInTime = parseTime(request.getClockInTime(), "clockInTime");
        LocalTime clockOutTime = parseTime(request.getClockOutTime(), "clockOutTime");
        if ("WORK".equals(status) && (clockInTime == null || clockOutTime == null)) {
            throw validationError("ERR03005", "出勤時刻と退勤時刻を入力してください", "clockInTime",
                    null, "WORKの場合は出勤時刻と退勤時刻が必須です");
        }
        if (clockInTime != null && clockOutTime != null && !clockOutTime.isAfter(clockInTime)) {
            throw validationError("ERR03001", "出勤時刻は退勤時刻より前である必要があります",
                    "clockInTime", request.getClockInTime(), "退勤時刻より前である必要があります");
        }

        AttendanceRecord record = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDate(employeeId, attendanceDate)
                .orElseGet(AttendanceRecord::new);
        record.setEmployeeId(employeeId);
        record.setAttendanceDate(attendanceDate);
        record.setClockInTime(clockInTime);
        record.setClockOutTime(clockOutTime);
        record.setStatus(status);
        record.setNotes(request.getNotes());
        return toResponse(attendanceRecordRepository.save(record));
    }

    /**
     * 従業員IDと日付で勤怠レコードを取得する
     */
    public Optional<AttendanceResponse> getAttendanceByDate(String employeeId, String date) {
        String validatedEmployeeId = validateEmployeeId(employeeId);
        LocalDate attendanceDate = parseAttendanceDate(date);
        return attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDate(validatedEmployeeId, attendanceDate)
                .map(this::toResponse);
    }

    /**
     * 従業員IDで勤怠レコード一覧を取得する
     * TODO: REQ-UC002 - 勤怠レコード一覧取得ロジックの実装
     */
    public List<AttendanceResponse> getAttendancesByEmployeeId(String employeeId) {
        // TODO: REQ-UC002 - 実装予定
        return List.of();
    }

    private String validateEmployeeId(String employeeId) {
        if (employeeId == null || !employeeId.matches("^EMP[0-9]{3}$")) {
            throw validationError("ERR03003", "従業員IDの形式が不正です", "employeeId",
                    employeeId, "EMPに続く3桁の数字で指定してください");
        }
        return employeeId;
    }

    private LocalDate parseAttendanceDate(String date) {
        if (date == null || !date.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}$")) {
            throw validationError("ERR03004", "有効な日付をYYYY-MM-DD形式で指定してください",
                    "attendanceDate", date, "YYYY-MM-DD形式の有効な日付を指定してください");
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException exception) {
            throw validationError("ERR03004", "有効な日付をYYYY-MM-DD形式で指定してください",
                    "attendanceDate", date, "YYYY-MM-DD形式の有効な日付を指定してください");
        }
    }

    private LocalTime parseTime(String value, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (!value.matches("^([01][0-9]|2[0-3]):[0-5][0-9]$")) {
            throw validationError("ERR03002", "有効な時刻形式で入力してください", field,
                    value, "HH:MM形式（00:00～23:59）で入力してください");
        }
        return LocalTime.parse(value);
    }

    private AttendanceApiException validationError(String code, String message, String field,
                                                   String rejectedValue, String reason) {
        Map<String, Object> details = new java.util.LinkedHashMap<>();
        details.put("field", field);
        if (rejectedValue != null) {
            details.put("rejectedValue", rejectedValue);
        }
        details.put("reason", reason);
        return new AttendanceApiException(HttpStatus.BAD_REQUEST, code, message, details);
    }

    private AttendanceResponse toResponse(AttendanceRecord record) {
        return new AttendanceResponse(
                record.getId(),
                record.getEmployeeId(),
                record.getAttendanceDate().toString(),
                record.getClockInTime() == null ? null : record.getClockInTime().format(TIME_FORMATTER),
                record.getClockOutTime() == null ? null : record.getClockOutTime().format(TIME_FORMATTER),
                record.getStatus(),
                record.getNotes(),
                formatDateTime(record.getCreatedAt()),
                formatDateTime(record.getUpdatedAt()));
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.format(DATE_TIME_FORMATTER);
    }
}
