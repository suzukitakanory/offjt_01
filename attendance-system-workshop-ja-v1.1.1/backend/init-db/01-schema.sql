-- 勤怠レコードテーブル
-- TODO: REQ-UC003, REQ-UC004 - テーブル構造の詳細定義

CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(50) NOT NULL,
    attendance_date DATE NOT NULL,
    clock_in_time TIME,
    clock_out_time TIME,
    status VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(employee_id, attendance_date)
);

-- インデックス
CREATE INDEX IF NOT EXISTS idx_attendance_employee_date 
    ON attendance_records(employee_id, attendance_date);
