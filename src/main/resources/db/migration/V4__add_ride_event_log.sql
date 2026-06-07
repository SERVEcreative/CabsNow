CREATE TABLE IF NOT EXISTS ride_event_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL UNIQUE,
    duty_id INT NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    status VARCHAR(20),
    payload_json TEXT,
    created_at DATETIME NOT NULL,
    INDEX idx_ride_event_duty (duty_id)
);
