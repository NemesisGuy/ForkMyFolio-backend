-- V1__Initial_Schema.sql

-- User and Security Related Tables
CREATE TABLE users
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid            VARCHAR(36)  NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255),
    first_name      VARCHAR(255),
    last_name       VARCHAR(255),
    slug            VARCHAR(255) NOT NULL UNIQUE,
    provider        VARCHAR(50),
    provider_id     VARCHAR(255),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    email_verified  BOOLEAN   DEFAULT FALSE,
    is_public       BOOLEAN   DEFAULT FALSE,
    is_locked       BOOLEAN   DEFAULT FALSE,
    is_enabled      BOOLEAN   DEFAULT TRUE,
    last_login_date TIMESTAMP
);

CREATE TABLE roles
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Portfolio Content Tables
CREATE TABLE profiles
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT UNIQUE NOT NULL,
    uuid               VARCHAR(36)   NOT NULL UNIQUE,
    headline           VARCHAR(255),
    subheading         VARCHAR(500),
    about_me           TEXT,
    profile_image_url  VARCHAR(2048),
    cover_image_url    VARCHAR(2048),
    phone_number       VARCHAR(50),
    address            VARCHAR(500),
    show_email         BOOLEAN DEFAULT TRUE,
    show_phone         BOOLEAN DEFAULT TRUE,
    show_address       BOOLEAN DEFAULT TRUE,
    show_linkedin      BOOLEAN DEFAULT TRUE,
    show_github        BOOLEAN DEFAULT TRUE,
    show_twitter       BOOLEAN DEFAULT TRUE,
    linkedin_url       VARCHAR(2048),
    github_url         VARCHAR(2048),
    twitter_url        VARCHAR(2048),
    website_url        VARCHAR(2048),
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE projects
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    uuid              VARCHAR(36)  NOT NULL UNIQUE,
    title             VARCHAR(255) NOT NULL,
    description       TEXT,
    project_url       VARCHAR(2048),
    source_code_url   VARCHAR(2048),
    start_date        DATE,
    end_date          DATE,
    display_order     INT,
    is_featured       BOOLEAN   DEFAULT FALSE,
    cover_image_url   VARCHAR(2048),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE skills
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    uuid          VARCHAR(36)  NOT NULL UNIQUE,
    name          VARCHAR(255) NOT NULL,
    proficiency   INT,
    category      VARCHAR(255),
    display_order INT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE project_skills
(
    project_id BIGINT NOT NULL,
    skill_id   BIGINT NOT NULL,
    PRIMARY KEY (project_id, skill_id),
    FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE CASCADE
);

CREATE TABLE experiences
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    uuid          VARCHAR(36)  NOT NULL UNIQUE,
    company       VARCHAR(255) NOT NULL,
    role          VARCHAR(255) NOT NULL,
    description   TEXT,
    start_date    DATE,
    end_date      DATE,
    location      VARCHAR(255),
    display_order INT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE qualifications
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    uuid          VARCHAR(36)  NOT NULL UNIQUE,
    institution   VARCHAR(255) NOT NULL,
    qualification VARCHAR(255) NOT NULL,
    description   TEXT,
    start_date    DATE,
    end_date      DATE,
    display_order INT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE testimonials
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    uuid            VARCHAR(36)  NOT NULL UNIQUE,
    author_name     VARCHAR(255) NOT NULL,
    author_role     VARCHAR(255),
    author_company  VARCHAR(255),
    testimonial     TEXT,
    display_order   INT,
    is_approved     BOOLEAN   DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Other Tables
CREATE TABLE contact_messages
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    uuid       VARCHAR(36)  NOT NULL UNIQUE,
    sender_name  VARCHAR(255) NOT NULL,
    sender_email VARCHAR(255) NOT NULL,
    subject    VARCHAR(255),
    message    TEXT,
    is_read    BOOLEAN   DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE settings
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT, -- Can be NULL for global settings
    uuid       VARCHAR(36)  NOT NULL UNIQUE,
    name       VARCHAR(255) NOT NULL,
    value      TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE (user_id, name),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE backups
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT, -- Can be NULL for system-wide backups
    uuid       VARCHAR(36)  NOT NULL UNIQUE,
    filename   VARCHAR(255) NOT NULL,
    file_path  VARCHAR(2048),
    file_size  BIGINT,
    status     VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE password_reset_tokens
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE email_verification_tokens
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);