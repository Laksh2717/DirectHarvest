CREATE TABLE ratings (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    rater_id BIGINT NOT NULL,
    rated_user_id BIGINT NOT NULL,
    score INTEGER NOT NULL,
    review VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_ratings_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_rater FOREIGN KEY (rater_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_rated_user FOREIGN KEY (rated_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_ratings_order_rater UNIQUE (order_id, rater_id),
    CONSTRAINT chk_ratings_score CHECK (score BETWEEN 1 AND 5),
    CONSTRAINT chk_ratings_rater_not_rated CHECK (rater_id <> rated_user_id)
);

CREATE INDEX idx_ratings_rated_user_id ON ratings (rated_user_id);
CREATE INDEX idx_ratings_order_id ON ratings (order_id);

