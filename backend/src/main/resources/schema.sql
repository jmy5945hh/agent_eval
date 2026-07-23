-- Create database
CREATE DATABASE IF NOT EXISTS agent_eval DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE agent_eval;

-- User table
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL COMMENT 'Username',
    email VARCHAR(100) NOT NULL COMMENT 'Email',
    phone VARCHAR(20) COMMENT 'Phone',
    status INT NOT NULL DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User table';
