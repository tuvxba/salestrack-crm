CREATE TABLE deals (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    stage VARCHAR(20) NOT NULL,
    expected_close_date DATE,
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_deals_company
        FOREIGN KEY (company_id)
        REFERENCES companies (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_deals_company_id ON deals (company_id);
CREATE INDEX idx_deals_stage ON deals (stage);