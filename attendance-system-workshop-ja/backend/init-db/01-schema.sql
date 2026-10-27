-- 勤怠レコードテーブル

CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGSERIAL CONSTRAINT pk_attendance_records PRIMARY KEY,
    employee_id VARCHAR(50) NOT NULL,
    attendance_date DATE NOT NULL,
    clock_in_time TIME,
    clock_out_time TIME,
    status VARCHAR(20) NOT NULL DEFAULT 'WORK',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_attendance_employee_date UNIQUE (employee_id, attendance_date),
    CONSTRAINT chk_attendance_status
        CHECK (status IN ('WORK', 'PAID_LEAVE', 'SICK_LEAVE', 'UNPAID_LEAVE', 'PERSONAL_LEAVE', 'BUSINESS_TRIP')),
    CONSTRAINT chk_attendance_clock_times
        CHECK (clock_out_time IS NULL OR clock_in_time IS NULL OR clock_out_time > clock_in_time),
    CONSTRAINT chk_attendance_work_times
        CHECK (status <> 'WORK' OR (clock_in_time IS NOT NULL AND clock_out_time IS NOT NULL)),
    CONSTRAINT chk_attendance_leave_times
        CHECK (
            status NOT IN ('PAID_LEAVE', 'SICK_LEAVE', 'UNPAID_LEAVE', 'PERSONAL_LEAVE', 'BUSINESS_TRIP')
            OR (clock_in_time IS NULL AND clock_out_time IS NULL)
        )
);
