package com.attendance.entity;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Optional;

/**
 * 休暇区分。画面と API の表示名を保存コードへ変換する。勤務の WORK は含めない。
 */
public enum LeaveType {
    SICK_LEAVE("Sick Leave"),
    PERSONAL_LEAVE("Personal Leave"),
    BUSINESS_TRIP("Business Trip");

    private final String displayName;

    LeaveType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStatusCode() {
        return name();
    }

    public static Optional<LeaveType> fromDisplayName(String displayName) {
        if (displayName == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(type -> type.displayName.equals(displayName))
                .findFirst();
    }

    public static Optional<LeaveType> fromStatusCode(String statusCode) {
        if (statusCode == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(type -> type.name().equals(statusCode))
                .findFirst();
    }

    public static boolean isLeaveStatus(String statusCode) {
        return fromStatusCode(statusCode).isPresent();
    }

    @Documented
    @Constraint(validatedBy = SupportedValidator.class)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Supported {
        String message() default "ERR03006";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    public static class SupportedValidator implements ConstraintValidator<Supported, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isBlank()) {
                return true;
            }
            return fromDisplayName(value).isPresent();
        }
    }
}
