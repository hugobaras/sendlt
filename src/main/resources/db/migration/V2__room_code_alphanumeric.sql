ALTER TABLE climbing_sessions
    DROP CONSTRAINT IF EXISTS chk_climbing_sessions_room_code_length;

ALTER TABLE climbing_sessions
    ADD CONSTRAINT chk_climbing_sessions_room_code_format
        CHECK (room_code ~ '^[A-Z0-9]{6}$');
