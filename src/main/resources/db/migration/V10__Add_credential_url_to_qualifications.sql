-- V10: Adds the credential_url column to the qualifications table.
-- This column was missing from the V8 migration and is needed to store
-- a link to the certificate or verification page for a qualification.
ALTER TABLE qualifications
    ADD COLUMN credential_url VARCHAR(2048) NULL COMMENT 'URL to the credential, certificate, or verification page';