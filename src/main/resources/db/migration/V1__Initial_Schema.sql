-- V1__Initial_Schema.sql
-- This script creates the complete initial database schema for the ForkMyFolio application.

-- User and Authentication Tables
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       uuid VARCHAR(36) NOT NULL UNIQUE,
                       slug VARCHAR(50) NOT NULL UNIQUE, -- This column was missing
                       email VARCHAR(255) NOT NULL UNIQUE,
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       profile_image_url VARCHAR(255),
                       active BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role VARCHAR(255) NOT NULL,
                            PRIMARY KEY (user_id, role),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE refresh_tokens (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                user_id BIGINT NOT NULL,
                                token VARCHAR(512) NOT NULL UNIQUE,
                                expiry_date TIMESTAMP NOT NULL,
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Portfolio Core Profile Table
CREATE TABLE portfolio_profiles (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    user_id BIGINT NOT NULL UNIQUE,
                                    visible BOOLEAN NOT NULL DEFAULT TRUE,
                                    headline VARCHAR(100),
                                    summary TEXT,
                                    public_email VARCHAR(255),
                                    website_url VARCHAR(255),
                                    linkedin_url VARCHAR(255),
                                    github_url VARCHAR(255),
                                    resume_url VARCHAR(255),
                                    resume_image_url VARCHAR(255),
                                    location VARCHAR(50),
                                    cover_letter_template TEXT,
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Portfolio Section Tables
CREATE TABLE projects (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          uuid VARCHAR(36) NOT NULL UNIQUE,
                          user_id BIGINT NOT NULL,
                          visible BOOLEAN NOT NULL DEFAULT TRUE,
                          title VARCHAR(100) NOT NULL,
                          description TEXT,
                          repo_url VARCHAR(255),
                          live_url VARCHAR(255),
                          image_url VARCHAR(255),
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE project_tech_stack (
                                    project_id BIGINT NOT NULL,
                                    technology VARCHAR(255),
                                    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE skills (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        uuid VARCHAR(36) NOT NULL UNIQUE,
                        user_id BIGINT NOT NULL,
                        visible BOOLEAN NOT NULL DEFAULT TRUE,
                        name VARCHAR(50) NOT NULL,
                        level VARCHAR(255) NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE experiences (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             uuid VARCHAR(36) NOT NULL UNIQUE,
                             user_id BIGINT NOT NULL,
                             visible BOOLEAN NOT NULL DEFAULT TRUE,
                             job_title VARCHAR(100) NOT NULL,
                             company_name VARCHAR(100) NOT NULL,
                             location VARCHAR(100),
                             start_date DATE NOT NULL,
                             end_date DATE,
                             description TEXT,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE qualifications (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                uuid VARCHAR(36) NOT NULL UNIQUE,
                                user_id BIGINT NOT NULL,
                                visible BOOLEAN NOT NULL DEFAULT TRUE,
                                qualification_name VARCHAR(255) NOT NULL,
                                institution_name VARCHAR(255) NOT NULL,
                                completion_year INT NOT NULL,
                                grade VARCHAR(255),
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE testimonials (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              uuid VARCHAR(36) NOT NULL UNIQUE,
                              user_id BIGINT NOT NULL,
                              visible BOOLEAN NOT NULL DEFAULT TRUE,
                              quote TEXT NOT NULL,
                              author_name VARCHAR(100) NOT NULL,
                              author_title VARCHAR(100),
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Application-level Tables
CREATE TABLE contact_messages (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  uuid VARCHAR(36) NOT NULL UNIQUE,
                                  user_id BIGINT NOT NULL,
                                  name VARCHAR(100) NOT NULL,
                                  email VARCHAR(255) NOT NULL,
                                  message TEXT NOT NULL,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE settings (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          uuid VARCHAR(36) NOT NULL UNIQUE,
                          name VARCHAR(255) NOT NULL UNIQUE,
                          value VARCHAR(255) NOT NULL,
                          description VARCHAR(512)
);

CREATE TABLE visitor_stats (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               type VARCHAR(100) NOT NULL,
                               ref_id VARCHAR(255) NOT NULL,
                               count BIGINT NOT NULL DEFAULT 0,
                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               UNIQUE KEY unique_stat (type, ref_id)
);