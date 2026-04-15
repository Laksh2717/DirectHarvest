ALTER TABLE orders
    ADD COLUMN cancelled_by VARCHAR(20),
    ADD COLUMN cancelled_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_orders_cancelled_by ON orders (cancelled_by);

