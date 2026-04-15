CREATE TABLE cloudinary_delete_outbox (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_error VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_cloudinary_delete_outbox_status_next_attempt
    ON cloudinary_delete_outbox (status, next_attempt_at);

CREATE INDEX idx_cloudinary_delete_outbox_public_id
    ON cloudinary_delete_outbox (public_id);

