package com.attendance.controller;

import com.attendance.dto.ApiErrorResponse;
import com.attendance.dto.CreateLeaveRequest;
import com.attendance.dto.LeaveResponse;
import com.attendance.dto.UpdateLeaveRequest;
import com.attendance.entity.LeaveType;
import com.attendance.exception.LeaveApiException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaveExceptionHandlerTest {

    private final LeaveExceptionHandler handler = new LeaveExceptionHandler();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void mapsMissingDateTypeMemoLimitAndUnsupportedTypeTo400() {
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleLeaveApiException(LeaveApiException.missingDate()).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleLeaveApiException(LeaveApiException.missingLeaveType()).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleLeaveApiException(LeaveApiException.unsupportedLeaveType()).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleLeaveApiException(LeaveApiException.memoTooLong()).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleLeaveApiException(LeaveApiException.incompletePeriod()).getStatusCode());
        assertEquals("休暇日を指定してください",
                handler.handleLeaveApiException(LeaveApiException.missingDate()).getBody().message());
        assertEquals(ApiErrorResponse.ERR03006,
                handler.handleLeaveApiException(LeaveApiException.unsupportedLeaveType()).getBody().error());
        assertEquals(ApiErrorResponse.ERR03008,
                handler.handleLeaveApiException(LeaveApiException.incompletePeriod()).getBody().error());
    }

    @Test
    void mapsDuplicateAndMissingConfirmationTo409AndMissingLeaveTo404() {
        ResponseEntity<ApiErrorResponse> duplicate = handler.handleLeaveApiException(LeaveApiException.duplicateLeave());
        ResponseEntity<ApiErrorResponse> confirmation =
                handler.handleLeaveApiException(LeaveApiException.replaceRequiresConfirmation());
        ResponseEntity<ApiErrorResponse> missing = handler.handleLeaveApiException(LeaveApiException.leaveNotFound());

        assertEquals(HttpStatus.CONFLICT, duplicate.getStatusCode());
        assertEquals(ApiErrorResponse.ERR02003, duplicate.getBody().error());
        assertEquals(HttpStatus.CONFLICT, confirmation.getStatusCode());
        assertEquals(ApiErrorResponse.ERR02005, confirmation.getBody().error());
        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
        assertEquals("指定した日付の休暇はありません", missing.getBody().message());
        assertFalse(missing.getBody().message().contains("Exception"));
    }

    @Test
    void rejectsErrorTextThatIsNotTheCatalogMessage() {
        assertThrows(IllegalArgumentException.class,
                () -> new ApiErrorResponse(ApiErrorResponse.ERR02002, "SQLException: secret"));
    }

    @Test
    void validatesCreateAndUpdateRequestsWithoutAcceptingEmployeeId() {
        CreateLeaveRequest missing = new CreateLeaveRequest();
        assertTrue(messages(validator.validate(missing)).contains(ApiErrorResponse.ERR03004));
        assertTrue(messages(validator.validate(missing)).contains(ApiErrorResponse.ERR03005));

        CreateLeaveRequest invalid = new CreateLeaveRequest(
                "2026-02-31", "WORK", "a".repeat(501), false);
        Set<String> invalidMessages = messages(validator.validate(invalid));
        assertTrue(invalidMessages.contains(ApiErrorResponse.ERR03004));
        assertTrue(invalidMessages.contains(ApiErrorResponse.ERR03006));
        assertTrue(invalidMessages.contains(ApiErrorResponse.ERR03007));

        CreateLeaveRequest valid = new CreateLeaveRequest("2026-11-09", "Sick Leave", null, null);
        assertTrue(validator.validate(valid).isEmpty());

        UpdateLeaveRequest update = new UpdateLeaveRequest("Personal Leave", "");
        assertTrue(validator.validate(update).isEmpty());
        assertTrue(validator.validate(new UpdateLeaveRequest("Business Trip extra", null))
                .stream().map(ConstraintViolation::getMessage).anyMatch(ApiErrorResponse.ERR03006::equals));
    }

    @Test
    void mapsDisplayNamesAndKeepsWorkOutOfLeaveTypes() {
        assertEquals("SICK_LEAVE", LeaveType.fromDisplayName("Sick Leave").orElseThrow().getStatusCode());
        assertEquals("Personal Leave", LeaveType.PERSONAL_LEAVE.getDisplayName());
        assertEquals("BUSINESS_TRIP", LeaveType.BUSINESS_TRIP.getStatusCode());
        assertFalse(LeaveType.isLeaveStatus("WORK"));
        assertEquals("", LeaveResponse.of("2026-11-09", LeaveType.SICK_LEAVE, null).getMemo());
    }

    private Set<String> messages(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }
}
