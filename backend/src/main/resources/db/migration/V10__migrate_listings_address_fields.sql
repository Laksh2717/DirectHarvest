-- Migrate listings table to match ODT spec
-- Remove: title, available_from
-- Replace: location with street, city, state, pincode

ALTER TABLE listings
DROP COLUMN IF EXISTS title,
DROP COLUMN IF EXISTS available_from,
DROP COLUMN IF EXISTS location,
ADD COLUMN street VARCHAR(255) NOT NULL DEFAULT '',
ADD COLUMN city VARCHAR(100) NOT NULL DEFAULT '',
ADD COLUMN state VARCHAR(100) NOT NULL DEFAULT '',
ADD COLUMN pincode VARCHAR(10) NOT NULL DEFAULT '';

-- Remove defaults after migration
ALTER TABLE listings
ALTER COLUMN street DROP DEFAULT,
ALTER COLUMN city DROP DEFAULT,
ALTER COLUMN state DROP DEFAULT,
ALTER COLUMN pincode DROP DEFAULT;

-- Create index for location queries
CREATE INDEX IF NOT EXISTS idx_listings_city_state ON listings (city, state);

