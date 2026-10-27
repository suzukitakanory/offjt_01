-- Apply once to an existing database after reviewing the data.
\set ON_ERROR_STOP on

BEGIN;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM attendance_records
        WHERE status IS NULL
           OR status NOT IN ('WORK', 'PAID_LEAVE', 'SICK_LEAVE', 'UNPAID_LEAVE')
           OR (
               status = 'WORK'
               AND (clock_in_time IS NULL OR clock_out_time IS NULL)
           )
           OR (
               clock_in_time IS NOT NULL
               AND clock_out_time IS NOT NULL
               AND clock_out_time <= clock_in_time
           )
    ) THEN
        RAISE EXCEPTION
            'Attendance data violates the new status or clock-time constraints; no rows were changed';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM attendance_records
        GROUP BY employee_id, attendance_date
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'Attendance data contains duplicate employee/date pairs; no rows were changed';
    END IF;
END;
$$;

ALTER TABLE attendance_records
    DROP CONSTRAINT IF EXISTS chk_attendance_status,
    DROP CONSTRAINT IF EXISTS chk_attendance_clock_times,
    DROP CONSTRAINT IF EXISTS chk_attendance_work_times;

ALTER TABLE attendance_records
    ALTER COLUMN status TYPE VARCHAR(20),
    ALTER COLUMN status SET DEFAULT 'WORK',
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE attendance_records
    ADD CONSTRAINT chk_attendance_status
        CHECK (status IN ('WORK', 'PAID_LEAVE', 'SICK_LEAVE', 'UNPAID_LEAVE')),
    ADD CONSTRAINT chk_attendance_clock_times
        CHECK (clock_out_time IS NULL OR clock_in_time IS NULL OR clock_out_time > clock_in_time),
    ADD CONSTRAINT chk_attendance_work_times
        CHECK (status <> 'WORK' OR (clock_in_time IS NOT NULL AND clock_out_time IS NOT NULL));

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'attendance_records'::regclass
          AND conname = 'attendance_records_employee_id_attendance_date_key'
    ) AND NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'attendance_records'::regclass
          AND conname = 'uk_attendance_employee_date'
    ) THEN
        ALTER TABLE attendance_records
            RENAME CONSTRAINT attendance_records_employee_id_attendance_date_key
            TO uk_attendance_employee_date;
    ELSIF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'attendance_records'::regclass
          AND conname = 'uk_attendance_employee_date'
    ) THEN
        ALTER TABLE attendance_records
            ADD CONSTRAINT uk_attendance_employee_date
            UNIQUE (employee_id, attendance_date);
    END IF;
END;
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'attendance_records'::regclass
          AND conname = 'attendance_records_pkey'
    ) AND NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'attendance_records'::regclass
          AND conname = 'pk_attendance_records'
    ) THEN
        ALTER TABLE attendance_records
            RENAME CONSTRAINT attendance_records_pkey TO pk_attendance_records;
    END IF;
END;
$$;

DROP INDEX IF EXISTS idx_attendance_employee_date;

COMMIT;
