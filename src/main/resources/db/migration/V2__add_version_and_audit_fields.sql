ALTER TABLE onboarding_users
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN last_modified_date TIMESTAMP WITHOUT TIME ZONE;

UPDATE onboarding_users
    SET last_modified_date = created_at
    WHERE last_modified_date IS NULL;
