-- Create employee item requests table
CREATE TABLE employee_item_requests (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL REFERENCES users(id),
    department_id BIGINT NOT NULL REFERENCES departments(id),
    status VARCHAR(20) NOT NULL,
    comments VARCHAR(1000),
    response_comments VARCHAR(1000),
    request_date TIMESTAMP NOT NULL,
    processed_date TIMESTAMP,
    processor_id BIGINT REFERENCES users(id)
);

-- Create employee request items table
CREATE TABLE employee_request_items (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES employee_item_requests(id),
    item_id BIGINT NOT NULL REFERENCES inventory_items(id),
    quantity INTEGER NOT NULL,
    comments VARCHAR(1000)
); 