CREATE TABLE payments (
    id               UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    crew_profile_id  UUID           NOT NULL REFERENCES crew_profiles(id),
    amount_pln       NUMERIC(10,2)  NOT NULL,
    currency         VARCHAR(3)     NOT NULL DEFAULT 'PLN',
    payment_provider VARCHAR(30)    NOT NULL,
    provider_tx_id   VARCHAR(255),
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    payment_type     VARCHAR(20)    NOT NULL CHECK (payment_type IN ('LISTING', 'BOOST')),
    reference_id     UUID,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    completed_at     TIMESTAMPTZ
);

CREATE UNIQUE INDEX idx_payments_provider_tx ON payments(provider_tx_id);
CREATE INDEX idx_payments_crew ON payments(crew_profile_id);
