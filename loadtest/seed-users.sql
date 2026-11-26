-- Load-test data for user-service (userdb). Idempotent-ish: only seeds when < 100 users.
DO $$
BEGIN
  IF (SELECT count(*) FROM users) < 100 THEN
    INSERT INTO users (username, name, email, password, points, created_at)
    SELECT 'loaduser' || g, 'Load User ' || g, 'loaduser' || g || '@example.com',
           '$2a$12$abcdefghijklmnopqrstuuvwxyzABCDEFGHIJKLMNOPQRSTUV12', (random() * 1000)::int, now()
    FROM generate_series(1, 500) g;
    INSERT INTO user_followers (follower_id, following_id, created_at)
    SELECT DISTINCT a, b, now() FROM (
      SELECT (random() * 499 + 1)::bigint a, (random() * 499 + 1)::bigint b FROM generate_series(1, 5000)
    ) s WHERE a <> b AND a IN (SELECT id FROM users) AND b IN (SELECT id FROM users)
    ON CONFLICT DO NOTHING;
  END IF;
END $$;
