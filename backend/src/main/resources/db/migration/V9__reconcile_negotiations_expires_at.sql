ALTER TABLE negotiations
    ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP WITH TIME ZONE;

UPDATE negotiations
SET expires_at = COALESCE(expires_at, created_at, NOW())
WHERE expires_at IS NULL;

ALTER TABLE negotiations
    ALTER COLUMN expires_at SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_negotiations_expires_at ON negotiations (expires_at);

