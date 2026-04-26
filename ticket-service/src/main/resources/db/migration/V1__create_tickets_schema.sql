CREATE TABLE tickets (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id     UUID         NOT NULL,
    user_id      UUID         NOT NULL,
    user_email   VARCHAR(100) NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    qr_code      TEXT,
    booked_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    cancelled_at TIMESTAMP,

    -- Un utilisateur ne peut avoir qu'un seul ticket actif par event
    CONSTRAINT uq_ticket_user_event UNIQUE (event_id, user_id)
);

CREATE INDEX idx_tickets_event  ON tickets(event_id);
CREATE INDEX idx_tickets_user   ON tickets(user_id);
CREATE INDEX idx_tickets_status ON tickets(status);
