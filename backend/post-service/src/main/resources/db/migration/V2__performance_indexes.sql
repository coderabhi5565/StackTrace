CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Latest feed and trending: only live published posts, newest first.
CREATE INDEX idx_posts_published_feed ON posts (published_at DESC, id DESC)
    WHERE status = 'PUBLISHED' AND deleted_at IS NULL;

-- Author pages ("my posts", profile posts, following feed).
CREATE INDEX idx_posts_author_status ON posts (author_id, status, published_at DESC)
    WHERE deleted_at IS NULL;

-- Scheduler: due scheduled posts.
CREATE INDEX idx_posts_scheduled ON posts (scheduled_publish_at)
    WHERE status = 'SCHEDULED';

-- Full-text search and partial-title fallback.
CREATE INDEX idx_posts_search_vector ON posts USING GIN (search_vector);
CREATE INDEX idx_posts_title_trgm ON posts USING GIN (title gin_trgm_ops);

-- Tag filter (PK is (post_id, tag_id); this serves tag -> posts).
CREATE INDEX idx_post_tag_tag ON post_tag (tag_id, post_id);

-- "My bookmarks", newest first.
CREATE INDEX idx_bookmarks_user_created ON bookmarks (user_id, created_at DESC);
