-- V9: Adds the email_verified column to the users table.
-- This column is necessary for both local and OAuth2 authentication flows
-- to track whether a user's email address has been confirmed.
ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Indicates if the user has verified their email address';