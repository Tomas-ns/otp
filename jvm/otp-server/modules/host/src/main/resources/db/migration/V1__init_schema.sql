-- OTP schema inicial (PostgreSQL 15+)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE stations (
    id              VARCHAR(64) PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    transport_type  VARCHAR(16) NOT NULL CHECK (transport_type IN ('METRO', 'TRAIN'))
);

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(320),
    display_name    VARCHAR(255),
    avatar_url      TEXT,
    status          VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'DISABLED', 'DELETED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_login_at   TIMESTAMPTZ
);

CREATE UNIQUE INDEX users_email_lower_unique_idx
    ON users (lower(email))
    WHERE email IS NOT NULL;

CREATE TABLE oauth_accounts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    provider            VARCHAR(32) NOT NULL CHECK (provider IN ('GOOGLE')),
    provider_subject    VARCHAR(255) NOT NULL,
    provider_email      VARCHAR(320),
    provider_username   VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_used_at        TIMESTAMPTZ,
    UNIQUE (provider, provider_subject)
);

CREATE INDEX oauth_accounts_user_id_idx ON oauth_accounts (user_id);

CREATE TABLE predictions (
    id               BIGSERIAL PRIMARY KEY,
    station_id       VARCHAR(64) NOT NULL REFERENCES stations (id),
    user_id          UUID REFERENCES users (id),
    occupancy_level  SMALLINT NOT NULL CHECK (occupancy_level BETWEEN 1 AND 5),
    type             VARCHAR(16) NOT NULL CHECK (type IN ('COMPLETE', 'LIMITED')),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT predictions_type_user_check CHECK (
        (type = 'COMPLETE' AND user_id IS NOT NULL)
        OR (type = 'LIMITED' AND user_id IS NULL)
    )
);

CREATE INDEX predictions_station_type_created_idx
    ON predictions (station_id, type, created_at DESC);

CREATE INDEX predictions_user_created_idx
    ON predictions (user_id, created_at DESC)
    WHERE user_id IS NOT NULL;

CREATE TABLE refresh_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash      CHAR(64) NOT NULL UNIQUE,
    expires_at      TIMESTAMPTZ NOT NULL,
    revoked_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    replaced_by_id  UUID REFERENCES refresh_tokens (id),
    user_agent      TEXT,
    ip_address      INET
);

CREATE INDEX refresh_tokens_user_id_idx ON refresh_tokens (user_id);
