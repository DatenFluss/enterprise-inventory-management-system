ALTER TABLE inventory_items
ADD COLUMN user_id BIGINT,
ADD COLUMN checked_out_at TIMESTAMP,
ADD COLUMN due_date TIMESTAMP,
ADD CONSTRAINT fk_inventory_items_user
    FOREIGN KEY (user_id)
    REFERENCES users (id); 