-- Add display_order column to the projects table for custom sorting
ALTER TABLE projects
    ADD COLUMN display_order INT NOT NULL DEFAULT 0;