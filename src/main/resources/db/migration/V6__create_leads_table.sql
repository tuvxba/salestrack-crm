CREATE TABLE leads (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    email VARCHAR(180),
    phone VARCHAR(40),
    company_name VARCHAR(160),
    source VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    assigned_user_id BIGINT NOT NULL,
    converted_deal_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_leads_assigned_user FOREIGN KEY (assigned_user_id) REFERENCES users (id),
    CONSTRAINT fk_leads_converted_deal FOREIGN KEY (converted_deal_id) REFERENCES deals (id)
);

CREATE INDEX idx_leads_assigned_user_id ON leads (assigned_user_id);
CREATE INDEX idx_leads_status ON leads (status);