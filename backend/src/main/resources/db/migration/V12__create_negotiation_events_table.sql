CREATE TABLE negotiation_events (
    id BIGSERIAL PRIMARY KEY,
    negotiation_id BIGINT NOT NULL,
    actor_user_id BIGINT NOT NULL,
    actor_role VARCHAR(20) NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    offered_price NUMERIC(12, 2) NOT NULL,
    requested_quantity NUMERIC(12, 2) NOT NULL,
    status_after VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_negotiation_events_negotiation
        FOREIGN KEY (negotiation_id) REFERENCES negotiations (id) ON DELETE CASCADE,
    CONSTRAINT fk_negotiation_events_actor
        FOREIGN KEY (actor_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_negotiation_events_offered_price_positive CHECK (offered_price > 0),
    CONSTRAINT chk_negotiation_events_requested_quantity_positive CHECK (requested_quantity > 0)
);

CREATE INDEX idx_negotiation_events_negotiation_id ON negotiation_events (negotiation_id);
CREATE INDEX idx_negotiation_events_created_at ON negotiation_events (created_at);

