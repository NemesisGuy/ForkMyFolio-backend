-- V12: Adds the institution_logo_url column to the qualifications table.
-- This column was missing from the V8 migration and is needed to store
-- a URL to the logo of the institution that awarded the qualification.
ALTER TABLE qualifications
    ADD COLUMN institution_logo_url VARCHAR(2048) NULL COMMENT 'URL to the logo of the institution';