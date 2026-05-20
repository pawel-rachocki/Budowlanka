CREATE TABLE reviews (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    crew_profile_id UUID        NOT NULL REFERENCES crew_profiles(id),
    author_user_id  UUID        NOT NULL REFERENCES users(id),
    rating          SMALLINT    NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment         TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(crew_profile_id, author_user_id)
);

CREATE INDEX idx_reviews_crew ON reviews(crew_profile_id);
