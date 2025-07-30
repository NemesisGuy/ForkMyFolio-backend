-- V2__Seed_Initial_Data.sql
-- This script seeds the database with essential initial data, such as the default admin user and application settings.

-- 1. Create the default administrator user
-- The password is 'password', hashed using BCrypt.
-- The UUID and slug are hardcoded for consistency.
INSERT INTO users (uuid, slug, email, first_name, last_name, password, active)
VALUES ('a1b2c3d4-e5f6-7890-1234-567890abcdef', 'admin-user', 'admin@gmail.com', 'Admin', 'User', '$2a$10$7tQQCxQNI2QW93DoeqRQQu4StUML/gMnilTwmfSAw2wLuyKvtlm2u', TRUE);

-- 2. Assign roles to the new user
-- We need to get the ID of the user we just created.
SET @admin_user_id = (SELECT id FROM users WHERE email = 'admin@gmail.com');

-- --- THIS IS THE FIX ---
-- An admin is also a user, so we grant both roles for full access.
INSERT INTO user_roles (user_id, role)
VALUES
    (@admin_user_id, 'USER'),
    (@admin_user_id, 'ADMIN');

-- 3. Create a default portfolio profile for the admin user
INSERT INTO portfolio_profiles (user_id, headline, summary)
VALUES (@admin_user_id, 'Default Administrator Profile', 'This is the default profile for the site administrator.');

-- 4. Seed the initial application settings
-- This replaces the logic previously in DataInitializer.java
INSERT INTO settings (uuid, name, value, description)
VALUES
    ('a1a1a1a1-b2b2-c3c3-d4d4-e5e5e5e5e5e5', 'DEFAULT_PDF_TEMPLATE', 'modern', 'The default template used for the public PDF download button.'),
    ('b2b2b2b2-c3c3-d4d4-e5e5-f6f6f6f6f6f6', 'SHOW_PROJECTS', 'true', 'Display the "Projects" section on the public site.'),
    ('c3c3c3c3-d4d4-e5e5-f6f6-a1a1a1a1a1a1', 'SHOW_SKILLS', 'true', 'Display the "Skills" section on the public site.'),
    ('d4d4d4d4-e5e5-f6f6-a1a1-b2b2b2b2b2b2', 'SHOW_EXPERIENCE', 'true', 'Display the "Experience" section on the public site.'),
    ('e5e5e5e5-f6f6-a1a1-b2b2-c3c3c3c3c3c3', 'SHOW_TESTIMONIALS', 'true', 'Display the "Testimonials" section on the public site.'),
    ('f6f6f6f6-a1a1-b2b2-c3c3-d4d4d4d4d4d4', 'SHOW_QUALIFICATIONS', 'true', 'Display the "Qualifications" section on the public site.'),
    ('a2a2a2a2-b3b3-c4c4-d5d5-e6e6e6e6e6e6', 'SHOW_CONTACT_FORM', 'true', 'Display the "Contact Me" section on the public site.');