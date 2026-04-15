-- Fix rating uniqueness constraint to be per order, not per listing.
-- Buyers should be able to rate farmers for each order, not just once per listing.

ALTER TABLE ratings
    DROP CONSTRAINT uk_ratings_listing_rater_rated_user;

ALTER TABLE ratings
    ADD CONSTRAINT uk_ratings_order_rater UNIQUE (order_id, rater_id);
