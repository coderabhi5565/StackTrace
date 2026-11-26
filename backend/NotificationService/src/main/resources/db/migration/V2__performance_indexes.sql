CREATE INDEX idx_notifications_recipient ON notifications (recipient_id, created_at DESC, id DESC);
CREATE INDEX idx_notifications_unread ON notifications (recipient_id) WHERE is_read = FALSE;
