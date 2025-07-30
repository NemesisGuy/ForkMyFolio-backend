-- Add columns to the users table to support OAuth2 authentication providers.
-- The 'provider' column will default to 'LOCAL' for all existing users.
ALTER TABLE users
    ADD COLUMN provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL';
ALTER TABLE users
    ADD COLUMN provider_id VARCHAR(255) NULL;

-- Make the password column nullable to accommodate users signing up via an external provider.
-- Note: The syntax for modifying a column to be nullable can vary between database systems.
-- This example uses syntax common in MySQL / H2.
-- For PostgreSQL, you would use: ALTER TABLE users ALTER COLUMN password DROP NOT NULL;
ALTER TABLE users MODIFY COLUMN password VARCHAR (255) NULL;