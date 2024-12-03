-- Ensure all department managers are in their own departments
INSERT INTO department_employees (department_id, user_id)
SELECT d.id, d.manager_id
FROM departments d
WHERE d.manager_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM department_employees de
    WHERE de.department_id = d.id
    AND de.user_id = d.manager_id
);

-- Ensure all users with department_id are in department_employees
INSERT INTO department_employees (department_id, user_id)
SELECT u.department_id, u.id
FROM users u
WHERE u.department_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM department_employees de
    WHERE de.department_id = u.department_id
    AND de.user_id = u.id
);

-- Set manager_id for users in departments where it's missing
UPDATE users u
SET manager_id = d.manager_id
FROM departments d
WHERE u.department_id = d.id
AND u.id != d.manager_id
AND u.manager_id IS NULL;

-- Ensure department managers have ROLE_MANAGER role
UPDATE users u
SET role_id = (SELECT id FROM roles WHERE name = 'ROLE_MANAGER')
FROM departments d
WHERE d.manager_id = u.id
AND u.role_id = (SELECT id FROM roles WHERE name = 'ROLE_EMPLOYEE');

-- Ensure users in departments have ROLE_EMPLOYEE if they don't have a higher role
UPDATE users u
SET role_id = (SELECT id FROM roles WHERE name = 'ROLE_EMPLOYEE')
WHERE u.department_id IS NOT NULL
AND u.role_id IS NULL; 