-- User Service H2 Database Table Structure

-- User table for storing user information
CREATE TABLE "t_user"
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY, -- Unique user ID
    username VARCHAR(255) UNIQUE NOT NULL,      -- Username, must be unique
    password VARCHAR(255)        NOT NULL,      -- Hashed password
    email    VARCHAR(255) UNIQUE NOT NULL,      -- Email, must be unique
    role     VARCHAR(50)         NOT NULL       -- User role (e.g., USER, ADMIN)
);

-- Access token table for user authentication
CREATE TABLE "t_access_token"
(
    token_id   BIGINT AUTO_INCREMENT PRIMARY KEY, -- Unique token ID
    user_id    BIGINT,                            -- Reference to the user
    token      VARCHAR(255) NOT NULL,             -- Token value
    expiration TIMESTAMP    NOT NULL,             -- Expiration time of the token
    FOREIGN KEY (user_id) REFERENCES t_user (id)   -- Foreign key reference to Users table
);

-- Refresh token table for managing user sessions
CREATE TABLE "t_refresh_token"
(
    refresh_token_id BIGINT AUTO_INCREMENT PRIMARY KEY, -- Unique refresh token ID
    user_id          BIGINT,                            -- Reference to the user
    refresh_token    VARCHAR(255) NOT NULL,             -- Refresh token value
    expiration       TIMESTAMP    NOT NULL,             -- Expiration time of the refresh token
    FOREIGN KEY (user_id) REFERENCES t_user (id)         -- Foreign key reference to Users table
);
