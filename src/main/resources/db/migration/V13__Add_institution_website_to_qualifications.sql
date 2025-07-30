-- V13: Adds the institution_website column to the qualifications table.
-- This column was missing from the V8 migration and is needed to store
-- a URL to the website of the institution that awarded the qualification.
ALTER TABLE qualifications
    ADD COLUMN institution_website VARCHAR(2048) NULL COMMENT 'URL to the website of the institution';