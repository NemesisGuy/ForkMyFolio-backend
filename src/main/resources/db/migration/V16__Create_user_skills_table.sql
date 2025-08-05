-- V16: Creates the user_skills join table to manage the many-to-many relationship
-- between users and skills, including user-specific attributes like proficiency level.

CREATE TABLE user_skills (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             user_id BIGINT NOT NULL,
                             skill_id BIGINT NOT NULL,
                             level VARCHAR(255) NOT NULL COMMENT 'User-specific proficiency level (e.g., BEGINNER, ADVANCED)',
                             visible BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether the skill is visible on the public portfolio',
                             description TEXT COMMENT 'User-specific notes or description for the skill',
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                             CONSTRAINT fk_user_skills_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                             CONSTRAINT fk_user_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
                             UNIQUE KEY uk_user_skill (user_id, skill_id) COMMENT 'A user can only have a specific skill once'
);