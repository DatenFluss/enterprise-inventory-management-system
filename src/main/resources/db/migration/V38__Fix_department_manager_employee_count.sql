-- Insert missing department-employee associations
INSERT INTO department_employees (department_id, user_id)
SELECT u.department_id, u.id
FROM users u
WHERE u.department_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM department_employees de
    WHERE de.department_id = u.department_id
    AND de.user_id = u.id
); 

-- Set department managers as managers for all users in their departments
UPDATE users u
SET manager_id = d.manager_id
FROM departments d
WHERE u.department_id = d.id
AND u.id != d.manager_id
AND u.manager_id IS NULL; 