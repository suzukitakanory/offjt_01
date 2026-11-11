package com.attendance.controller;

import com.attendance.dto.CreateLeaveRequest;
import com.attendance.dto.LeaveResponse;
import com.attendance.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping
    public ResponseEntity<LeaveResponse> create(@Valid @RequestBody CreateLeaveRequest request) {
        LeaveService.CreationResult result = leaveService.create(request);
        HttpStatus status = result.replacedWork() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(result.response());
    }

    @GetMapping("/{date}")
    public LeaveResponse getByDate(@PathVariable String date) {
        return leaveService.getByDate(date);
    }
}
