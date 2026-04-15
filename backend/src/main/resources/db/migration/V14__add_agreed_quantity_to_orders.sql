ALTER TABLE orders
    ADD COLUMN agreed_quantity NUMERIC(12, 2);

UPDATE orders o
SET agreed_quantity = n.requested_quantity
FROM negotiations n
WHERE o.negotiation_id = n.id
  AND o.agreed_quantity IS NULL;

ALTER TABLE orders
    ALTER COLUMN agreed_quantity SET NOT NULL;

