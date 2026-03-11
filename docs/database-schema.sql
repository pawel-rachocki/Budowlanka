-- ============================================================
-- Portal Ekipy Remontowe — Schemat bazy danych PostgreSQL (MVP)
-- ============================================================
-- Źródło prawdy: ta tabela. Zmiany TYLKO przez migracje Flyway.
-- ============================================================

-- UŻYTKOWNICY I AUTORYZACJA

CREATE TABLE users (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email              VARCHAR(255) NOT NULL UNIQUE,
    password_hash      VARCHAR(255) NOT NULL,
    role               VARCHAR(20)  NOT NULL CHECK (role IN ('CLIENT', 'CREW', 'ADMIN')),
    email_verified     BOOLEAN      NOT NULL DEFAULT FALSE,
    verification_token VARCHAR(128),
    token_expires_at   TIMESTAMPTZ,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE refresh_tokens (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(512) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);

-- ============================================================
-- PROFILE EKIP REMONTOWYCH

CREATE TABLE crew_profiles (
    id                BIGSERIAL    PRIMARY KEY,
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

-- ============================================================
-- SPECJALIZACJE / KATEGORIE USŁUG

CREATE TABLE service_categories (
    id   SERIAL       PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE crew_services (
    crew_profile_id BIGINT NOT NULL REFERENCES crew_profiles(id) ON DELETE CASCADE,
    category_id     INT    NOT NULL REFERENCES service_categories(id),
    PRIMARY KEY (crew_profile_id, category_id)
);

-- ============================================================
-- PORTFOLIO ZDJĘĆ + MODERACJA

CREATE TABLE portfolio_photos (
    id                BIGSERIAL    PRIMARY KEY,
    crew_profile_id   BIGINT       NOT NULL REFERENCES crew_profiles(id) ON DELETE CASCADE,
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

-- ============================================================
-- OPINIE / RECENZJE

CREATE TABLE reviews (
    id              BIGSERIAL   PRIMARY KEY,
    crew_profile_id BIGINT      NOT NULL REFERENCES crew_profiles(id),
    author_user_id  UUID        NOT NULL REFERENCES users(id),
    rating          SMALLINT    NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment         TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(crew_profile_id, author_user_id)
);

CREATE INDEX idx_reviews_crew ON reviews(crew_profile_id);

-- ============================================================
-- PAKIETY I PŁATNOŚCI (MONETYZACJA)

CREATE TABLE listing_packages (
    id            SERIAL         PRIMARY KEY,
    name          VARCHAR(50)    NOT NULL,
    duration_days INT            NOT NULL,
    price_pln     NUMERIC(10,2)  NOT NULL,
    is_active     BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE TABLE boost_packages (
    id            SERIAL         PRIMARY KEY,
    name          VARCHAR(50)    NOT NULL,
    duration_days INT            NOT NULL,
    price_pln     NUMERIC(10,2)  NOT NULL,
    is_active     BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE TABLE crew_subscriptions (
    id              BIGSERIAL   PRIMARY KEY,
    crew_profile_id BIGINT      NOT NULL REFERENCES crew_profiles(id),
    package_id      INT         NOT NULL REFERENCES listing_packages(id),
    starts_at       TIMESTAMPTZ NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subs_crew ON crew_subscriptions(crew_profile_id);
CREATE INDEX idx_subs_expires ON crew_subscriptions(expires_at);

CREATE TABLE crew_boosts (
    id               BIGSERIAL   PRIMARY KEY,
    crew_profile_id  BIGINT      NOT NULL REFERENCES crew_profiles(id),
    boost_package_id INT         NOT NULL REFERENCES boost_packages(id),
    starts_at        TIMESTAMPTZ NOT NULL,
    expires_at       TIMESTAMPTZ NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE payments (
    id               BIGSERIAL      PRIMARY KEY,
    crew_profile_id  BIGINT         NOT NULL REFERENCES crew_profiles(id),
    amount_pln       NUMERIC(10,2)  NOT NULL,
    currency         VARCHAR(3)     NOT NULL DEFAULT 'PLN',
    payment_provider VARCHAR(30)    NOT NULL,
    provider_tx_id   VARCHAR(255),
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    payment_type     VARCHAR(20)    NOT NULL CHECK (payment_type IN ('LISTING', 'BOOST')),
    reference_id     BIGINT,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    completed_at     TIMESTAMPTZ
);

-- ============================================================
-- PRZYSZŁY CZAT (Faza 2+) — tabele gotowe, API nie implementuj teraz

CREATE TABLE conversations (
    id             BIGSERIAL   PRIMARY KEY,
    client_user_id UUID        NOT NULL REFERENCES users(id),
    crew_user_id   UUID        NOT NULL REFERENCES users(id),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(client_user_id, crew_user_id)
);

CREATE TABLE messages (
    id              BIGSERIAL   PRIMARY KEY,
    conversation_id BIGINT      NOT NULL REFERENCES conversations(id),
    sender_user_id  UUID        NOT NULL REFERENCES users(id),
    content         TEXT        NOT NULL,
    is_read         BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_conv ON messages(conversation_id, created_at);
