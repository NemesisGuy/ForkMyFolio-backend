-- Add new columns to the experiences table to support enhanced features
ALTER TABLE experiences
    ADD COLUMN company_url VARCHAR(255) NULL,
    ADD COLUMN company_logo_url VARCHAR(255) NULL,
    ADD COLUMN location_type VARCHAR(255) NULL,
    ADD COLUMN employment_type VARCHAR(255) NULL,
    ADD COLUMN achievements TEXT NULL,
    ADD COLUMN display_order INT NOT NULL DEFAULT 0;

-- Create the join table for the ManyToMany relationship between Experience and Skill
CREATE TABLE experience_skills
(
    experience_id BIGINT NOT NULL,
    skill_id      BIGINT NOT NULL,
    PRIMARY KEY (experience_id, skill_id),
    CONSTRAINT fk_experience_skills_experience FOREIGN KEY (experience_id) REFERENCES experiences (id) ON DELETE CASCADE,
    CONSTRAINT fk_experience_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE CASCADE
);