CREATE TABLE outbox_events (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type    text        NOT NULL,
    routing_key   text        NOT NULL,
    aggregation_id uuid       NOT NULL,
    payload       text        NOT NULL,
    status        varchar(10) NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW','PUBLISHED','FAILED')),
    attempts      integer     NOT NULL DEFAULT 0,
    available_at  timestamp   NOT NULL DEFAULT now(),
    published_at  timestamp,
    created_at    timestamp   NOT NULL DEFAULT now()
);
CREATE INDEX ix_outbox_pending ON outbox_events (status, available_at)
    WHERE status IN ('NEW','FAILED');
