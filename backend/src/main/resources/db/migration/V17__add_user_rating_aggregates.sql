ALTER TABLE users
    ADD COLUMN average_rating NUMERIC(4, 2),
    ADD COLUMN rating_count INTEGER NOT NULL DEFAULT 0;

UPDATE users u
SET rating_count = COALESCE(r.cnt, 0),
    average_rating = r.avg_score
FROM (
    SELECT rated_user_id,
           COUNT(*) AS cnt,
           ROUND(AVG(score)::numeric, 2) AS avg_score
    FROM ratings
    GROUP BY rated_user_id
) r
WHERE u.id = r.rated_user_id;

