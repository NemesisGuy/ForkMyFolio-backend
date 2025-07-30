-- Drop the old table that stored the tech stack as a simple list of strings.
-- The IF EXISTS clause prevents an error if the table doesn't exist (e.g., in a fresh setup).
DROP TABLE IF EXISTS project_tech_stack;

-- Create the new join table for the ManyToMany relationship between Project and Skill.
CREATE TABLE project_skills
(
    project_id BIGINT NOT NULL,
    skill_id   BIGINT NOT NULL,
    PRIMARY KEY (project_id, skill_id),
    CONSTRAINT fk_project_skills_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_project_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE CASCADE
);