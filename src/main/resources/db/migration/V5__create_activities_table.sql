CREATE TABLE activities (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    deal_id BIGINT,
    contact_id BIGINT,
    logged_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_activities_deal FOREIGN KEY (deal_id) REFERENCES deals (id) ON DELETE CASCADE,
    CONSTRAINT fk_activities_contact FOREIGN KEY (contact_id) REFERENCES contacts (id) ON DELETE CASCADE,
    CONSTRAINT fk_activities_logged_by FOREIGN KEY (logged_by_user_id) REFERENCES users (id),
    CONSTRAINT chk_activity_target CHECK (
        (deal_id IS NOT NULL AND contact_id IS NULL) OR
        (deal_id IS NULL AND contact_id IS NOT NULL)
    )
);

CREATE INDEX idx_activities_deal_id ON activities (deal_id);
CREATE INDEX idx_activities_contact_id ON activities (contact_id);