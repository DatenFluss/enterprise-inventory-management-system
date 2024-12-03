-- Remove testuser3's department association
UPDATE users 
SET department_id = NULL 
WHERE username = 'testuser3';

-- Clean up department_employees table
DELETE FROM department_employees
WHERE user_id IN (
    SELECT id FROM users WHERE username = 'testuser3'
);

-- Ensure managers are properly associated
INSERT INTO department_employees (department_id, user_id)
SELECT d.id, d.manager_id
FROM departments d
WHERE d.manager_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM department_employees de
    WHERE de.department_id = d.id
    AND de.user_id = d.manager_id
); 