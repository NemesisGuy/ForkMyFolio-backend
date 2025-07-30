-- V8: Enhances the qualifications table with additional details.
-- The 'grade' column was found to be a duplicate during migration and has been removed from this script.
-- It likely exists from a previous, un-versioned change or an earlier version of this script.
ALTER TABLE qualifications
    ADD COLUMN start_date DATE COMMENT 'The start date of the qualification period',
    ADD COLUMN end_date DATE COMMENT 'The end date or award date of the qualification';