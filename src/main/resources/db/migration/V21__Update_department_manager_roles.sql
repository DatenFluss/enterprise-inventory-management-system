-- Get the manager role ID
DO $$
DECLARE
    manager_role_id bigint;
BEGIN
    -- Get the manager role ID
    SELECT id INTO manager_role_id FROM roles WHERE name = 'ROLE_MANAGER';

    -- Update users who are department managers but don't have manager role
    UPDATE users u
    SET role_id = manager_role_id
    FROM departments d
    WHERE d.manager_id = u.id
    AND u.role_id IN (SELECT id FROM roles WHERE name = 'ROLE_EMPLOYEE');
END $$; 