CREATE TABLE listing_packages (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(50)   NOT NULL,
    duration_days INT           NOT NULL,
    price_pln     NUMERIC(10,2) NOT NULL,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE boost_packages (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(50)   NOT NULL,
    duration_days INT           NOT NULL,
    price_pln     NUMERIC(10,2) NOT NULL,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE crew_subscriptions (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    crew_profile_id UUID        NOT NULL REFERENCES crew_profiles(id),
    package_id      UUID        NOT NULL REFERENCES listing_packages(id),
    starts_at       TIMESTAMPTZ NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subs_crew ON crew_subscriptions(crew_profile_id);
CREATE INDEX idx_subs_expires ON crew_subscriptions(expires_at);

CREATE TABLE crew_boosts (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    crew_profile_id  UUID        NOT NULL REFERENCES crew_profiles(id),
    boost_package_id UUID        NOT NULL REFERENCES boost_packages(id),
    starts_at        TIMESTAMPTZ NOT NULL,
    expires_at       TIMESTAMPTZ NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_boosts_crew ON crew_boosts(crew_profile_id);
CREATE INDEX idx_boosts_expires ON crew_boosts(expires_at);
