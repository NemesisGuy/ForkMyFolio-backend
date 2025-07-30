-- V11: Adds the field_of_study column to the qualifications table.
-- This column was missing from the V8 migration and is needed to store
-- the specific field of study for a qualification.
ALTER TABLE qualifications
    ADD COLUMN field_of_study VARCHAR(255) NULL COMMENT 'The field of study for the qualification, e.g., Computer Science';