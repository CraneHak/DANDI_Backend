CREATE TABLE IF NOT EXISTS collection_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lost_item_id INT NOT NULL,
    pickup_pass_id BIGINT NULL,
    requester_uid VARCHAR(128) NOT NULL,
    requester_email VARCHAR(255) NOT NULL,
    manager_uid VARCHAR(128) NULL,
    manager_email VARCHAR(255) NULL,
    otp_token VARCHAR(120) NOT NULL,
    otp_expires_at DATETIME(6) NOT NULL,
    collected_at DATETIME(6) NULL,
    action VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_collection_log_lost_item
        FOREIGN KEY (lost_item_id) REFERENCES lost_item(id)
) CHARACTER SET utf8mb4;

CREATE INDEX IF NOT EXISTS idx_collection_log_lost_item_id ON collection_log(lost_item_id);
CREATE INDEX IF NOT EXISTS idx_collection_log_pickup_pass_id ON collection_log(pickup_pass_id);
CREATE INDEX IF NOT EXISTS idx_collection_log_requester_uid ON collection_log(requester_uid);
CREATE INDEX IF NOT EXISTS idx_collection_log_action ON collection_log(action);
