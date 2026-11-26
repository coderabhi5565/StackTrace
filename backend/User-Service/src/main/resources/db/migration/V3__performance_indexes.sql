-- Followers / following lists ordered by recency (the unique key covers follower_id lookups only).
CREATE INDEX idx_follow_following_created ON user_followers (following_id, created_at DESC);
CREATE INDEX idx_follow_follower_created ON user_followers (follower_id, created_at DESC);

-- Leaderboard and rank (count of users with more points).
CREATE INDEX idx_users_points ON users (points DESC, id);

-- Fuzzy user search (ILIKE + similarity) via trigrams.
CREATE INDEX idx_users_username_trgm ON users USING GIN (username gin_trgm_ops);
CREATE INDEX idx_users_name_trgm ON users USING GIN (name gin_trgm_ops);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX idx_user_badges_user ON user_badges (user_id);
