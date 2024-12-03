-- Handle the case where either department_id or target_department_id exists
DO $$
BEGIN
    -- Check if department_id exists and target_department_id doesn't exist
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'item_requests'
        AND column_name = 'department_id'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'item_requests'
        AND column_name = 'target_department_id'
    ) THEN
        ALTER TABLE item_requests RENAME COLUMN department_id TO target_department_id;
    END IF;
END $$; 