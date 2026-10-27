-- Add UC004 leave statuses and enforce null clock times for leave records.
\set ON_ERROR_STOP on

BEGIN;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM attendance_records
        WHERE status IS NULL
           OR status NOT IN (
               'WORK',
               'PAID_LEAVE',
               'SICK_LEAVE',
               'UNPAID_LEAVE',
               'PERSONAL_LEAVE',
               'BUSINESS_TRIP'
           )
           OR (
               status = 'WORK'
               AND (clock_in_time IS NULL OR clock_out_time IS NULL)
           )
           OR (
               status IN (
                   'PAID_LEAVE',
                   'SICK_LEAVE',
                   'UNPAID_LEAVE',
                   'PERSONAL_LEAVE',
                   'BUSINESS_TRIP'
               )
               AND (clock_in_time IS NOT NULL OR clock_out_time IS NOT NULL)
           )
           OR (
               clock_in_time IS NOT NULL
               AND clock_out_time IS NOT NULL
               AND clock_out_time <= clock_in_time
           )
    ) THEN
        RAISE EXCEPTION
            'Attendance data violates UC004 status or clock-time constraints; no rows were changed';
    END IF;
END;
$$;

ALTER TABLE attendance_records
    DROP CONSTRAINT IF EXISTS chk_attendance_status,
    DROP CONSTRAINT IF EXISTS chk_attendance_leave_times;

ALTER TABLE attendance_records
    ADD CONSTRAINT chk_attendance_status
        CHECK (status IN (
            'WORK',
            'PAID_LEAVE',
            'SICK_LEAVE',
            'UNPAID_LEAVE',
            'PERSONAL_LEAVE',
            'BUSINESS_TRIP'
        )),
    ADD CONSTRAINT chk_attendance_leave_times
        CHECK (
            status NOT IN ('PAID_LEAVE', 'SICK_LEAVE', 'UNPAID_LEAVE', 'PERSONAL_LEAVE', 'BUSINESS_TRIP')
            OR (clock_in_time IS NULL AND clock_out_time IS NULL)
        );

COMMIT;
