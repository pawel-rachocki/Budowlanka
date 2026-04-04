--V005 create crew_profiles table

CREATE TABLE crew_profiles (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID         NOT NULL UNIQUE REFERENCES users(id),
    company_name      VARCHAR(255) NOT NULL,
    slug              VARCHAR(255) NOT NULL UNIQUE,
    description       TEXT,
    phone             VARCHAR(20),
    contact_email     VARCHAR(255),
    city              VARCHAR(100) NOT NULL,
    voivodeship       VARCHAR(50)  NOT NULL,
    service_radius_km INT          DEFAULT 50,
    nip               VARCHAR(10),
    avg_rating        NUMERIC(3,2) DEFAULT 0,
    review_count      INT          NOT NULL DEFAULT 0,
    is_visible        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_crew_city ON crew_profiles(city);
CREATE INDEX idx_crew_voivodeship ON crew_profiles(voivodeship);
