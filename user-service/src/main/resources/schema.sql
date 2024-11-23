-- User Service H2 Database Table Structure

-- User table for storing user information
CREATE TABLE "T_USER"
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY, -- Unique user ID
    username VARCHAR(255) UNIQUE NOT NULL,      -- Username, must be unique
    password VARCHAR(255)        NOT NULL,      -- Hashed password
    email    VARCHAR(255) UNIQUE NOT NULL,      -- Email, must be unique
    role     VARCHAR(50)         NOT NULL,      -- User role (e.g., USER, ADMIN)
    create_time TIMESTAMP          NOT NULL        -- Record creation time
);

-- Access token table for user authentication
CREATE TABLE "T_ACCESS_TOKEN"
(
    token_id   BIGINT AUTO_INCREMENT PRIMARY KEY, -- Unique token ID
    user_id    BIGINT,                            -- Reference to the user
    token      VARCHAR(255) NOT NULL,             -- Token value
    expiration TIMESTAMP    NOT NULL,             -- Expiration time of the token
    create_time TIMESTAMP    NOT NULL,             -- Record creation time
    FOREIGN KEY (user_id) REFERENCES T_USER (id)   -- Foreign key reference to Users table
);

-- Refresh token table for managing user sessions
CREATE TABLE "T_REFRESH_TOKEN"
(
    refresh_token_id BIGINT AUTO_INCREMENT PRIMARY KEY, -- Unique refresh token ID
    user_id          BIGINT,                            -- Reference to the user
    refresh_token    VARCHAR(255) NOT NULL,             -- Refresh token value
    expiration       TIMESTAMP    NOT NULL,             -- Expiration time of the refresh token
    create_time      TIMESTAMP    NOT NULL,             -- Record creation time
    FOREIGN KEY (user_id) REFERENCES T_USER (id)         -- Foreign key reference to Users table
);