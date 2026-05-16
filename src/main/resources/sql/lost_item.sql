ALTER TABLE lost_item
    ADD COLUMN IF NOT EXISTS category_id INT NULL AFTER item_type,
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'STORED' AFTER category_id,
    ADD COLUMN IF NOT EXISTS masking_flag TINYINT(1) NOT NULL DEFAULT 0 AFTER status,
    ADD COLUMN IF NOT EXISTS lost_location VARCHAR(255) NULL AFTER found_location,
    ADD COLUMN IF NOT EXISTS vision_dominant_colors_json TEXT NULL AFTER image_url,
    ADD COLUMN IF NOT EXISTS vision_extracted_text TEXT NULL AFTER vision_dominant_colors_json,
    ADD COLUMN IF NOT EXISTS created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER vision_extracted_text,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

SET @fk_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'lost_item'
      AND CONSTRAINT_NAME = 'fk_lost_item_category'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @fk_sql := IF(
    @fk_exists = 0,
    'ALTER TABLE lost_item ADD CONSTRAINT fk_lost_item_category FOREIGN KEY (category_id) REFERENCES category(id)',
    'SELECT 1'
);
PREPARE stmt FROM @fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX IF NOT EXISTS idx_lost_item_status ON lost_item(status);
CREATE INDEX IF NOT EXISTS idx_lost_item_category_id ON lost_item(category_id);
