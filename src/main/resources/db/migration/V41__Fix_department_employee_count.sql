-- Remove any duplicate entries in department_employees
DELETE FROM department_employees a USING (
    SELECT MIN(ctid) as ctid, department_id, user_id
    FROM department_employees 
    GROUP BY department_id, user_id
    HAVING COUNT(*) > 1
) b
WHERE a.department_id = b.department_id 
AND a.user_id = b.user_id 
AND a.ctid <> b.ctid;

-- Remove entries where user is no longer in the department
DELETE FROM department_employees de
WHERE NOT EXISTS (
    SELECT 1 FROM users u
    WHERE u.id = de.user_id
    AND u.department_id = de.department_id
);

-- Add missing entries
INSERT INTO department_employees (department_id, user_id)
SELECT u.department_id, u.id
FROM users u
WHERE u.department_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM department_employees de
    WHERE de.department_id = u.department_id
    AND de.user_id = u.id
); 