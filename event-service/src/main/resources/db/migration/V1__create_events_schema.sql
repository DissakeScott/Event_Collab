CREATE TABLE events (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    title            VARCHAR(150)  NOT NULL,
    description      TEXT,
    location         VARCHAR(255)  NOT NULL,
    start_date       TIMESTAMP     NOT NULL,
    end_date         TIMESTAMP     NOT NULL,
    max_capacity     INT           NOT NULL,
    current_capacity INT           NOT NULL DEFAULT 0,
    status           VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    organizer_id     UUID          NOT NULL,
    organizer_email  VARCHAR(100)  NOT NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP,

    CONSTRAINT chk_capacity CHECK (current_capacity >= 0),
    CONSTRAINT chk_max      CHECK (max_capacity > 0),
    CONSTRAINT chk_dates    CHECK (end_date > start_date)
);

CREATE INDEX idx_events_organizer ON events(organizer_id);
CREATE INDEX idx_events_status    ON events(status);
CREATE INDEX idx_events_start     ON events(start_date);
