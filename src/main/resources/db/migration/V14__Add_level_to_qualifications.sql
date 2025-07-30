-- V14: Adds the level column to the qualifications table.
-- This column was missing from the V8 migration and is needed to store
-- the academic or professional level of the qualification (e.g., "Bachelor's Degree").
ALTER TABLE qualifications
    ADD COLUMN `level` VARCHAR(255) NULL COMMENT 'The level of the qualification, e.g., Bachelor''s Degree';