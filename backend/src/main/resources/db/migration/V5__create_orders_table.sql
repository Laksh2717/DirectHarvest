CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    negotiation_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    farmer_id BIGINT NOT NULL,
    agreed_price NUMERIC(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_orders_listing FOREIGN KEY (listing_id) REFERENCES listings (id) ON DELETE CASCADE,
    CONSTRAINT fk_orders_negotiation FOREIGN KEY (negotiation_id) REFERENCES negotiations (id) ON DELETE CASCADE,
    CONSTRAINT fk_orders_buyer FOREIGN KEY (buyer_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_orders_farmer FOREIGN KEY (farmer_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_orders_negotiation UNIQUE (negotiation_id),
    CONSTRAINT chk_orders_participants CHECK (buyer_id <> farmer_id)
);

CREATE INDEX idx_orders_buyer_id ON orders (buyer_id);
CREATE INDEX idx_orders_farmer_id ON orders (farmer_id);
CREATE INDEX idx_orders_status ON orders (status);

