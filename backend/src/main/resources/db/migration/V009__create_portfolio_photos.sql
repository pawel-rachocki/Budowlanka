CREATE TABLE portfolio_photos (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    crew_profile_id   UUID         NOT NULL REFERENCES crew_profiles(id) ON DELETE CASCADE,
    storage_key       VARCHAR(512) NOT NULL,
    thumbnail_key     VARCHAR(512),
    caption           VARCHAR(255),
    moderation_status VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
        CHECK (moderation_status IN ('PENDING', 'APPROVED', 'REJECTED')),
    moderation_note   TEXT,
    uploaded_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_photos_crew ON portfolio_photos(crew_profile_id);
CREATE INDEX idx_photos_moderation ON portfolio_photos(moderation_status);
