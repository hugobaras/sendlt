-- ON DELETE RESTRICT partout : un attempt ne doit pas disparaître si on drop un bloc.

CREATE TABLE gyms (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(255) NOT NULL,
    city          VARCHAR(255),
    scoring_mode  VARCHAR(32)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE sectors (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    gym_id     UUID         NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_sectors_gym
        FOREIGN KEY (gym_id) REFERENCES gyms (id) ON DELETE RESTRICT
);

CREATE INDEX idx_sectors_gym_id ON sectors (gym_id);

CREATE TABLE boulders (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    color         VARCHAR(32) NOT NULL,
    grade_font    VARCHAR(16),
    grade_v_scale VARCHAR(16),
    hold_style    VARCHAR(32) NOT NULL,
    opened_at     DATE        NOT NULL,
    sector_id     UUID        NOT NULL,
    CONSTRAINT fk_boulders_sector
        FOREIGN KEY (sector_id) REFERENCES sectors (id) ON DELETE RESTRICT
);

CREATE INDEX idx_boulders_sector_id ON boulders (sector_id);

CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(32)  NOT NULL,
    gym_id        UUID,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_gym
        FOREIGN KEY (gym_id) REFERENCES gyms (id) ON DELETE RESTRICT
);

CREATE INDEX idx_users_gym_id ON users (gym_id);

CREATE TABLE climbing_sessions (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_code  VARCHAR(6)   NOT NULL,
    status     VARCHAR(16)  NOT NULL,
    gym_id     UUID         NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    closed_at  TIMESTAMPTZ,
    CONSTRAINT uk_climbing_sessions_room_code UNIQUE (room_code),
    CONSTRAINT chk_climbing_sessions_room_code_length CHECK (char_length(room_code) = 6),
    CONSTRAINT fk_climbing_sessions_gym
        FOREIGN KEY (gym_id) REFERENCES gyms (id) ON DELETE RESTRICT
);

CREATE INDEX idx_climbing_sessions_gym_id ON climbing_sessions (gym_id);

CREATE TABLE session_participants (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID        NOT NULL,
    user_id    UUID        NOT NULL,
    joined_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_session_participants_session_user UNIQUE (session_id, user_id),
    CONSTRAINT fk_session_participants_session
        FOREIGN KEY (session_id) REFERENCES climbing_sessions (id) ON DELETE RESTRICT,
    CONSTRAINT fk_session_participants_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_session_participants_user_id ON session_participants (user_id);

CREATE TABLE attempts (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    boulder_id  UUID        NOT NULL,
    user_id     UUID        NOT NULL,
    session_id  UUID        NOT NULL,
    type        VARCHAR(32) NOT NULL,
    tries_count INTEGER     NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_attempts_tries_count_positive CHECK (tries_count >= 1),
    CONSTRAINT fk_attempts_boulder
        FOREIGN KEY (boulder_id) REFERENCES boulders (id) ON DELETE RESTRICT,
    CONSTRAINT fk_attempts_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_attempts_session
        FOREIGN KEY (session_id) REFERENCES climbing_sessions (id) ON DELETE RESTRICT
);

CREATE INDEX idx_attempts_boulder_session ON attempts (boulder_id, session_id);
CREATE INDEX idx_attempts_user_id ON attempts (user_id);
