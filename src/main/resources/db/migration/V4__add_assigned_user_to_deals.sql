ALTER TABLE deals
    ADD COLUMN assigned_user_id BIGINT;

ALTER TABLE deals
    ADD CONSTRAINT fk_deals_assigned_user
        FOREIGN KEY (assigned_user_id)
        REFERENCES users (id);

CREATE INDEX idx_deals_assigned_user_id ON deals (assigned_user_id);