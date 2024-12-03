-- First, clear any existing entries
DELETE FROM department_employees;

-- Insert entries for all users who have a department_id
INSERT INTO department_employees (department_id, user_id)
SELECT DISTINCT u.department_id, u.id
FROM users u
WHERE u.department_id IS NOT NULL;

-- Create index for better performance
CREATE INDEX IF NOT EXISTS idx_department_employees_dept_id ON department_employees(department_id);
CREATE INDEX IF NOT EXISTS idx_department_employees_user_id ON department_employees(user_id); 