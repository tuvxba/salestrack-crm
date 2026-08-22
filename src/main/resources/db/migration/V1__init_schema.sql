CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL UNIQUE,
    sector VARCHAR(100),
    website VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE contacts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    phone VARCHAR(40),
    position VARCHAR(120),
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_contacts_company
        FOREIGN KEY (company_id)
        REFERENCES companies (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_contacts_company_id ON contacts (company_id);