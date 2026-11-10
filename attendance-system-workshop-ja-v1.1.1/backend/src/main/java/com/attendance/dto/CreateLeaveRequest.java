package com.attendance.dto;

import com.attendance.entity.LeaveType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * 休暇の作成要求。従業員 ID は受け取らない。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateLeaveRequest {

    @NotBlank(message = "ERR03004")
    @IsoDate
    private String date;

    @NotBlank(message = "ERR03005")
    @LeaveType.Supported
    private String leaveType;

    @Size(max = 500, message = "ERR03007")
    private String memo;

    private Boolean confirmReplaceWork;

    @Documented
    @Constraint(validatedBy = IsoDate.Validator.class)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IsoDate {
        String message() default "ERR03004";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};

        class Validator implements ConstraintValidator<IsoDate, String> {
            @Override
            public boolean isValid(String value, ConstraintValidatorContext context) {
                if (value == null || value.isBlank()) {
                    return true;
                }
                if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    return false;
                }
                try {
                    LocalDate.parse(value);
                    return true;
                } catch (DateTimeParseException exception) {
                    return false;
                }
            }
        }
    }
}
