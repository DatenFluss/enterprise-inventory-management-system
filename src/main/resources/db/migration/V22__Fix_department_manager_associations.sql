-- Create the department_employees table if it doesn't exist
CREATE TABLE IF NOT EXISTS department_employees (
    department_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (department_id, user_id),
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Add managers to their departments' employee lists
INSERT INTO department_employees (department_id, user_id)
SELECT d.id, d.manager_id
FROM departments d
WHERE d.manager_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 
    FROM department_employees de 
    WHERE de.department_id = d.id 
    AND de.user_id = d.manager_id
);

-- Update users' department references
UPDATE users u
SET department_id = d.id
FROM departments d
WHERE d.manager_id = u.id
AND u.department_id IS NULL; 