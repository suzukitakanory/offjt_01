-- 勤怠レコードテーブル
-- REQ-UC003, REQ-UC004。新しいテーブルは作らない。
-- 休暇行は出勤・退勤の両方 NULL を許す。両方あるときだけ退勤が出勤より後であることを制約する。

CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(50) NOT NULL,
    attendance_date DATE NOT NULL,
    clock_in_time TIME,
    clock_out_time TIME,
    status VARCHAR(50) NOT NULL DEFAULT 'WORK',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_attendance_employee_date UNIQUE(employee_id, attendance_date),
    CONSTRAINT chk_attendance_status
        CHECK (status IN ('WORK', 'PAID_LEAVE', 'SICK_LEAVE', 'UNPAID_LEAVE', 'PERSONAL_LEAVE', 'BUSINESS_TRIP')),
    CONSTRAINT chk_attendance_clock_times
        CHECK (clock_out_time IS NULL OR clock_in_time IS NULL OR clock_out_time > clock_in_time)
);

ALTER TABLE attendance_records DROP CONSTRAINT IF EXISTS chk_attendance_status;
ALTER TABLE attendance_records
    ADD CONSTRAINT chk_attendance_status
    CHECK (status IN ('WORK', 'PAID_LEAVE', 'SICK_LEAVE', 'UNPAID_LEAVE', 'PERSONAL_LEAVE', 'BUSINESS_TRIP'));

ALTER TABLE attendance_records DROP CONSTRAINT IF EXISTS chk_attendance_clock_times;
ALTER TABLE attendance_records
    ADD CONSTRAINT chk_attendance_clock_times
    CHECK (clock_out_time IS NULL OR clock_in_time IS NULL OR clock_out_time > clock_in_time);

-- インデックス
CREATE INDEX IF NOT EXISTS idx_attendance_employee_date 
    ON attendance_records(employee_id, attendance_date);
