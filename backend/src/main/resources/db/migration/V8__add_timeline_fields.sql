ALTER TABLE negotiations
    ADD COLUMN expires_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE orders
    ADD COLUMN activated_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_negotiations_expires_at ON negotiations (expires_at);
CREATE INDEX idx_orders_activated_at ON orders (activated_at);
CREATE INDEX idx_orders_completed_at ON orders (completed_at);

