ALTER TABLE negotiations
    ADD COLUMN cancellation_reason VARCHAR(500),
    ADD COLUMN cancelled_by VARCHAR(20);

CREATE INDEX idx_negotiations_cancelled_by ON negotiations (cancelled_by);
