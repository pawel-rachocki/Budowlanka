--V004__add_unique_active_refresh_token_per_user.sql

-- Enforce at DB level: at most one active (non-revoked) refresh token per user
CREATE UNIQUE INDEX uq_refresh_tokens_active_user
    ON refresh_tokens(user_id)
    WHERE revoked = false;
