CREATE TABLE IF NOT EXISTS category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    ocr_target TINYINT(1) NOT NULL DEFAULT 0
) CHARACTER SET utf8mb4;

INSERT INTO category (name, ocr_target)
VALUES
    ('전자기기', 0),
    ('지갑', 0),
    ('카드', 1),
    ('신분증', 1),
    ('가방', 0),
    ('의류', 0)
ON DUPLICATE KEY UPDATE
    ocr_target = VALUES(ocr_target);
