-- V15: Adds the final missing columns to the qualifications table.
-- The 'completion_year' column was found to already exist, so it has been removed from this script.
ALTER TABLE qualifications
    ADD COLUMN start_year INT NULL COMMENT 'The year the qualification was started',
     ADD COLUMN still_studying BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Indicates if the user is still studying for this qualification';