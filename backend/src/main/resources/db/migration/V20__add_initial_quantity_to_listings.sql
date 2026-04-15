ALTER TABLE listings
    ADD COLUMN IF NOT EXISTS initial_quantity NUMERIC(12, 2);

UPDATE listings
SET initial_quantity = quantity
WHERE initial_quantity IS NULL;

ALTER TABLE listings
    ALTER COLUMN initial_quantity SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_listings_initial_quantity_positive'
    ) THEN
        ALTER TABLE listings
            ADD CONSTRAINT chk_listings_initial_quantity_positive CHECK (initial_quantity > 0);
    END IF;
END $$;
