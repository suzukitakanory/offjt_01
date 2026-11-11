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

    @Transactional(readOnly = true)
    public LeaveResponse getByDate(String value) {
        LocalDate date = parseDate(value);
        AttendanceRecord record = repository.findByEmployeeIdAndAttendanceDate(employeeId, date)
                .orElseThrow(LeaveApiException::leaveNotFound);
        LeaveType leaveType = LeaveType.fromStatusCode(record.getStatus())
                .orElseThrow(LeaveApiException::leaveNotFound);
        return LeaveResponse.of(date.toString(), leaveType, record.getNotes());
    }

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

    public record CreationResult(LeaveResponse response, boolean replacedWork) {
    }
}
