package com.attendance.controller;

import com.attendance.dto.CreateLeaveRequest;
import com.attendance.dto.LeaveResponse;
import com.attendance.dto.UpdateLeaveRequest;
import com.attendance.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    /**
     * 休暇を作成し、新規登録は201、確認済み勤務行の置換は200を返す。
     */
    @PostMapping
    public ResponseEntity<LeaveResponse> create(@Valid @RequestBody CreateLeaveRequest request) {
        LeaveService.CreationResult result = leaveService.create(request);
        HttpStatus status = result.replacedWork() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(result.response());
    }

    /**
     * 指定日の休暇を取得する。
     */
    @GetMapping("/{date}")
    public LeaveResponse getByDate(@PathVariable String date) {
        return leaveService.getByDate(date);
    }

    /**
     * 指定日の休暇を更新する。
     */
    @PutMapping("/{date}")
    public ResponseEntity<LeaveResponse> update(@PathVariable String date, @Valid @RequestBody UpdateLeaveRequest request) {
        LeaveResponse response = leaveService.update(date, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 指定日の休暇を削除する。
     */
    @DeleteMapping("/{date}")
    public ResponseEntity<Void> delete(@PathVariable String date) {
        leaveService.delete(date);
        return ResponseEntity.noContent().build();
    }
}
