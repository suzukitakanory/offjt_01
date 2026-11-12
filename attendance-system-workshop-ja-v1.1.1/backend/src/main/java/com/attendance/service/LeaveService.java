package com.attendance.service;

import com.attendance.dto.CreateLeaveRequest;
import com.attendance.dto.LeaveResponse;
import com.attendance.entity.AttendanceRecord;
import com.attendance.entity.LeaveType;
import com.attendance.exception.LeaveApiException;
import com.attendance.repository.AttendanceRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class LeaveService {

    private final AttendanceRecordRepository repository;
    private final String employeeId;

    public LeaveService(
            AttendanceRecordRepository repository,
            @Value("${attendance.demo-employee-id}") String employeeId) {
        this.repository = repository;
        this.employeeId = employeeId;
    }

    /**
     * 入力を検証して休暇を保存する。勤務記録は確認済みの場合だけ同じ行を置換する。
     *
     * @param request 休暇登録要求
     * @return 保存した休暇と勤務記録を置換したかどうか
     */
    @Transactional
    public CreationResult create(CreateLeaveRequest request) {
        if (request == null) {
            throw LeaveApiException.missingDate();
        }
        LocalDate date = parseDate(request.getDate());
        if (request.getLeaveType() == null || request.getLeaveType().isBlank()) {
            throw LeaveApiException.missingLeaveType();
        }
        LeaveType leaveType = LeaveType.fromDisplayName(request.getLeaveType())
                .orElseThrow(LeaveApiException::unsupportedLeaveType);
        String memo = request.getMemo();
        if (memo != null && memo.length() > 500) {
            throw LeaveApiException.memoTooLong();
        }
        if (memo != null && memo.isBlank()) {
            memo = null;
        }

        AttendanceRecord record = repository.findByEmployeeIdAndAttendanceDate(employeeId, date)
                .orElse(null);
        if (record == null) {
            record = new AttendanceRecord();
            record.setEmployeeId(employeeId);
            record.setAttendanceDate(date);
            record.setStatus(leaveType.getStatusCode());
            record.setNotes(memo);
            repository.save(record);
            return new CreationResult(LeaveResponse.of(date.toString(), leaveType, memo), false);
        }
        if (LeaveType.isLeaveStatus(record.getStatus())) {
            throw LeaveApiException.duplicateLeave();
        }
        if (!Boolean.TRUE.equals(request.getConfirmReplaceWork())) {
            throw LeaveApiException.replaceRequiresConfirmation();
        }

        record.setStatus(leaveType.getStatusCode());
        record.setClockInTime(null);
        record.setClockOutTime(null);
        record.setNotes(memo);
        repository.save(record);
        return new CreationResult(LeaveResponse.of(date.toString(), leaveType, memo), true);
    }

    /**
     * デモ従業員の指定日レコードを取得し、休暇でない行は未検出として扱う。
     *
     * @param value 休暇日（ISO形式）
     * @return 指定日の休暇
     */
    @Transactional(readOnly = true)
    public LeaveResponse getByDate(String value) {
        LocalDate date = parseDate(value);
        AttendanceRecord record = repository.findByEmployeeIdAndAttendanceDate(employeeId, date)
                .orElseThrow(LeaveApiException::leaveNotFound);
        return toLeaveResponse(record);
    }

    /**
     * デモ従業員の休暇だけを取得する。期間を指定する場合は開始日と終了日の両方を必須にする。
     */
    @Transactional(readOnly = true)
    public List<LeaveResponse> getLeaves(String from, String to) {
        if ((from == null) != (to == null)) {
            throw LeaveApiException.incompletePeriod();
        }

        List<AttendanceRecord> records;
        if (from == null) {
            records = repository.findByEmployeeId(employeeId);
        } else {
            records = repository.findByEmployeeIdAndAttendanceDateBetween(
                    employeeId, parsePeriodDate(from), parsePeriodDate(to));
        }

        return records.stream()
                .filter(record -> LeaveType.isLeaveStatus(record.getStatus()))
                .map(this::toLeaveResponse)
                .toList();
    }

    /**
     * 日付文字列を解析し、不足または不正な値を休暇日エラーに変換する。
     */
    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            throw LeaveApiException.missingDate();
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw LeaveApiException.missingDate();
        }
    }

    private LocalDate parsePeriodDate(String value) {
        if (value.isBlank()) {
            throw LeaveApiException.incompletePeriod();
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw LeaveApiException.incompletePeriod();
        }
    }

    private LeaveResponse toLeaveResponse(AttendanceRecord record) {
        LeaveType leaveType = LeaveType.fromStatusCode(record.getStatus())
                .orElseThrow(LeaveApiException::leaveNotFound);
        return LeaveResponse.of(record.getAttendanceDate().toString(), leaveType, record.getNotes());
    }

    /**
     * 指定日の休暇行だけを更新する。対象がなければレコードを作成しない。
     *
     * @param date 更新対象日（ISO形式）
     * @param request 更新要求
     * @return 更新した休暇
     */
    @Transactional
    public LeaveResponse update(String date, com.attendance.dto.UpdateLeaveRequest request) {
        throw new UnsupportedOperationException("update は未実装です（T024 で実装予定）");
    }

    /**
     * 指定日の休暇行だけを削除する。対象がなければ何も削除しない。
     *
     * @param date 削除対象日（ISO形式）
     */
    @Transactional
    public void delete(String date) {
        throw new UnsupportedOperationException("delete は未実装です（T031 で実装予定）");
    }

    /** 保存結果と勤務記録を置換したかどうかを表す。 */
    public record CreationResult(LeaveResponse response, boolean replacedWork) {
    }
}
