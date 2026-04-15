ALTER TABLE negotiation_events
    DROP CONSTRAINT IF EXISTS fk_negotiation_events_actor;

ALTER TABLE negotiation_events
    ALTER COLUMN actor_user_id DROP NOT NULL;

ALTER TABLE negotiation_events
    ALTER COLUMN actor_role DROP NOT NULL;

