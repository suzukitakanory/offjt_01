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
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 勤怠サービス
 * 関連要件: REQ-UC002 - カレンダー操作, REQ-UC003 - 勤務時間入力, REQ-UC004 - 休暇登録
 */
@Service
@Transactional
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;

    public AttendanceService(AttendanceRecordRepository attendanceRecordRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    /**
     * 勤怠レコードを保存する
     */
    public AttendanceResponse saveAttendance(AttendanceRequest request) {
        if (request.getEmployeeId() == null || !request.getEmployeeId().matches("EMP[0-9]{3}")) {
            throw new AttendanceApiException(HttpStatus.BAD_REQUEST, "ERR03004", "従業員IDの形式が不正です");
        }
        LocalDate date;
        try {
            date = LocalDate.parse(request.getAttendanceDate());
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new AttendanceApiException(HttpStatus.BAD_REQUEST, "ERR03005", "勤怠日の形式が不正です");
        }
        if (request.getClockInTime() == null || request.getClockInTime().isBlank()
                || request.getClockOutTime() == null || request.getClockOutTime().isBlank()) {
            throw new AttendanceApiException(HttpStatus.BAD_REQUEST, "ERR03003",
                    "出勤時刻と退勤時刻を入力してください");
        }
        if (!request.getClockInTime().matches("([01][0-9]|2[0-3]):[0-5][0-9]")
                || !request.getClockOutTime().matches("([01][0-9]|2[0-3]):[0-5][0-9]")) {
            throw new AttendanceApiException(HttpStatus.BAD_REQUEST, "ERR03002",
                    "有効な時刻形式で入力してください");
        }
        LocalTime clockIn = LocalTime.parse(request.getClockInTime());
        LocalTime clockOut = LocalTime.parse(request.getClockOutTime());
        if (!clockIn.isBefore(clockOut)) {
            throw new AttendanceApiException(HttpStatus.BAD_REQUEST, "ERR03001",
                    "出勤時刻は退勤時刻より前である必要があります");
        }
        AttendanceRecord record = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDate(request.getEmployeeId(), date)
                .orElseGet(AttendanceRecord::new);
        record.setEmployeeId(request.getEmployeeId());
        record.setAttendanceDate(date);
        record.setClockInTime(clockIn);
        record.setClockOutTime(clockOut);
        record.setStatus("WORK");
        AttendanceRecord saved = attendanceRecordRepository.save(record);
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        return new AttendanceResponse(saved.getId(), saved.getEmployeeId(), saved.getAttendanceDate().toString(),
                saved.getClockInTime().format(timeFormat), saved.getClockOutTime().format(timeFormat),
                saved.getStatus(), saved.getNotes(),
                saved.getCreatedAt() == null ? null : saved.getCreatedAt().toString(),
                saved.getUpdatedAt() == null ? null : saved.getUpdatedAt().toString());
    }

    /**
     * 従業員IDと日付で勤怠レコードを取得する
     */
    public Optional<AttendanceResponse> getAttendanceByDate(String employeeId, String date) {
        validateEmployeeId(employeeId);
        LocalDate attendanceDate = parseDate(date);
        return attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(employeeId, attendanceDate)
                .map(this::toResponse);
    }

    /**
     * 従業員IDで勤怠レコード一覧を取得する
     */
    public List<AttendanceResponse> getAttendancesByEmployeeId(String employeeId) {
        return getAttendancesByEmployeeId(employeeId, null, null);
    }

    /**
     * 従業員IDと任意の日付範囲で勤怠レコード一覧を取得する。
     *
     * @param employeeId 従業員ID
     * @param startDate 取得開始日（境界を含む）
     * @param endDate 取得終了日（境界を含む）
     * @return 条件に一致する勤怠レコード
     */
    public List<AttendanceResponse> getAttendancesByEmployeeId(
            String employeeId, String startDate, String endDate) {
        validateEmployeeId(employeeId);
        LocalDate start = startDate == null ? null : parseDate(startDate);
        LocalDate end = endDate == null ? null : parseDate(endDate);
        if (start != null && end != null && start.isAfter(end)) {
            return List.of();
        }
        List<AttendanceRecord> records;
        if (start != null && end != null) {
            records = attendanceRecordRepository.findByEmployeeIdAndAttendanceDateBetween(employeeId, start, end);
        } else if (start != null) {
            records = attendanceRecordRepository.findByEmployeeIdAndAttendanceDateGreaterThanEqual(employeeId, start);
        } else if (end != null) {
            records = attendanceRecordRepository.findByEmployeeIdAndAttendanceDateLessThanEqual(employeeId, end);
        } else {
            records = attendanceRecordRepository.findByEmployeeId(employeeId);
        }
        return records.stream().map(this::toResponse).toList();
    }

    private void validateEmployeeId(String employeeId) {
        if (employeeId == null || !employeeId.matches("EMP[0-9]{3}")) {
            throw new AttendanceApiException(HttpStatus.BAD_REQUEST, "ERR03004", "従業員IDの形式が不正です");
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new AttendanceApiException(HttpStatus.BAD_REQUEST, "ERR03005", "勤怠日の形式が不正です");
        }
    }

    private AttendanceResponse toResponse(AttendanceRecord record) {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        return new AttendanceResponse(record.getId(), record.getEmployeeId(), record.getAttendanceDate().toString(),
                record.getClockInTime() == null ? null : record.getClockInTime().format(timeFormat),
                record.getClockOutTime() == null ? null : record.getClockOutTime().format(timeFormat),
                record.getStatus(), record.getNotes(),
                record.getCreatedAt() == null ? null : record.getCreatedAt().toString(),
                record.getUpdatedAt() == null ? null : record.getUpdatedAt().toString());
    }
}
