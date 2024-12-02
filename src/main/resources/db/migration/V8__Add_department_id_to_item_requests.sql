ALTER TABLE item_requests ADD COLUMN department_id BIGINT;
ALTER TABLE item_requests ADD CONSTRAINT fk_item_requests_department FOREIGN KEY (department_id) REFERENCES departments(id); 