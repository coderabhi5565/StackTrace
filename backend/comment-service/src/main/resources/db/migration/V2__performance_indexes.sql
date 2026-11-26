-- Top-level comments of a post, newest first.
CREATE INDEX idx_comments_post_roots ON comments (post_id, created_at DESC, id DESC)
    WHERE parent_comment_id IS NULL;
-- Whole threads in one query.
CREATE INDEX idx_comments_root ON comments (root_comment_id, created_at, id);
-- Direct replies.
CREATE INDEX idx_comments_parent ON comments (parent_comment_id, created_at);
