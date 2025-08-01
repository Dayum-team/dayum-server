-- Create database if not exists
CREATE DATABASE IF NOT EXISTS dayum_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the database
USE dayum_db;

-- Create samples table if not exists
CREATE TABLE IF NOT EXISTS samples (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

-- Insert sample data
INSERT INTO samples (title, content, created_at, updated_at) VALUES
('첫 번째 샘플', '이것은 첫 번째 샘플 데이터입니다.', NOW(), NOW()),
('두 번째 샘플', '이것은 두 번째 샘플 데이터입니다.', NOW(), NOW()),
('테스트 데이터', 'API 테스트를 위한 샘플 데이터입니다.', NOW(), NOW()); 
