-- Set department managers as managers for all users in their departments
UPDATE users u
SET manager_id = d.manager_id
FROM departments d
WHERE u.department_id = d.id
AND u.id != d.manager_id
AND u.manager_id IS NULL; 