CREATE TABLE negotiations (
    id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    farmer_id BIGINT NOT NULL,
    offered_price NUMERIC(12, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    proposed_by VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_negotiations_listing FOREIGN KEY (listing_id) REFERENCES listings (id) ON DELETE CASCADE,
    CONSTRAINT fk_negotiations_buyer FOREIGN KEY (buyer_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_negotiations_farmer FOREIGN KEY (farmer_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_negotiations_participants CHECK (buyer_id <> farmer_id)
);

CREATE INDEX idx_negotiations_listing_id ON negotiations (listing_id);
CREATE INDEX idx_negotiations_buyer_id ON negotiations (buyer_id);
CREATE INDEX idx_negotiations_farmer_id ON negotiations (farmer_id);
CREATE INDEX idx_negotiations_status ON negotiations (status);

