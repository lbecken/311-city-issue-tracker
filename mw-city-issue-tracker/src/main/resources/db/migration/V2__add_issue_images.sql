-- Migration V2: Add issue_images table for storing image metadata
-- The actual image files are stored in the local filesystem

CREATE TABLE IF NOT EXISTS issue_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    issue_id UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_file_size CHECK (file_size > 0),
    CONSTRAINT chk_content_type CHECK (content_type IN ('image/jpeg', 'image/png', 'image/gif', 'image/webp'))
);

-- Index for faster lookups by issue
CREATE INDEX idx_issue_images_issue_id ON issue_images(issue_id);

-- Index for ordering by creation time
CREATE INDEX idx_issue_images_created_at ON issue_images(created_at);

COMMENT ON TABLE issue_images IS 'Stores metadata for images uploaded by citizens when reporting issues';
COMMENT ON COLUMN issue_images.filename IS 'Generated unique filename (UUID + extension)';
COMMENT ON COLUMN issue_images.original_filename IS 'Original filename from user upload';
COMMENT ON COLUMN issue_images.file_path IS 'Relative path to the stored file';
