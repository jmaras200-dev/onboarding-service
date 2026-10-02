CREATE TABLE onboarding_users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    national_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT uk_onboarding_users_email UNIQUE (email),
    CONSTRAINT uk_onboarding_users_national_id UNIQUE (national_id)
);
