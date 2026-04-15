ALTER TABLE negotiations
    ADD COLUMN IF NOT EXISTS requested_quantity NUMERIC(12, 2);

UPDATE negotiations n
SET requested_quantity = GREATEST(COALESCE(l.quantity, 0.01), 0.01)
FROM listings l
WHERE n.listing_id = l.id
  AND n.requested_quantity IS NULL;

ALTER TABLE negotiations
    ALTER COLUMN requested_quantity SET NOT NULL;

ALTER TABLE negotiations
    ADD CONSTRAINT chk_negotiations_requested_quantity_positive
        CHECK (requested_quantity > 0);

