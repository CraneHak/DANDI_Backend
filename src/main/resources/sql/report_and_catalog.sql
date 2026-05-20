-- 프론트 연동 스펙 (5/19) — report storage, lost_item ↔ report 연결, 이미지 URL 길이
ALTER TABLE report
    ADD COLUMN IF NOT EXISTS storage VARCHAR(255) NULL AFTER location;

ALTER TABLE lost_item
    ADD COLUMN IF NOT EXISTS report_id BIGINT NULL AFTER post_no,
    MODIFY COLUMN image_url VARCHAR(1024) NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_lost_item_report_id ON lost_item (report_id);
