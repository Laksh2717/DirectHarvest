ALTER TABLE ratings
    ADD COLUMN listing_id BIGINT;

UPDATE ratings r
SET listing_id = o.listing_id
FROM orders o
WHERE r.order_id = o.id;

ALTER TABLE ratings
    ALTER COLUMN listing_id SET NOT NULL;

ALTER TABLE ratings
    ADD CONSTRAINT fk_ratings_listing FOREIGN KEY (listing_id) REFERENCES listings (id) ON DELETE CASCADE;

ALTER TABLE ratings
    DROP CONSTRAINT IF EXISTS uk_ratings_order_rater;

ALTER TABLE ratings
    ADD CONSTRAINT uk_ratings_listing_rater_rated_user UNIQUE (listing_id, rater_id, rated_user_id);

CREATE INDEX idx_ratings_listing_id ON ratings (listing_id);

ALTER TABLE ratings
    DROP COLUMN IF EXISTS review;

