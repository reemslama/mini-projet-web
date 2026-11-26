CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE verifications (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id     uuid        NOT NULL,
    challenge_id      uuid,
    applicant_id      uuid,
    repo_url          text        NOT NULL,
    commit_sha        varchar(40) NOT NULL,
    branch            text        NOT NULL DEFAULT 'main',
    test_command      text,
    spec_digest       varchar(64),
    idempotency_key   varchar(64) NOT NULL,
    status            varchar(10) NOT NULL CHECK (status IN ('QUEUED','RUNNING','COMPLETED','ERROR')),
    result            varchar(4)           CHECK (result IN ('PASS','FAIL')),
    passed            integer     NOT NULL DEFAULT 0,
    failed            integer     NOT NULL DEFAULT 0,
    total             integer     NOT NULL DEFAULT 0,
    coverage          double precision,
    duration_ms       bigint      NOT NULL DEFAULT 0,
    sandbox_image     text,
    error_code        text,
    error_message     text,
    attempt           integer     NOT NULL DEFAULT 1,
    logs              text,
    requested_by      text,
    created_at        timestamp,
    started_at        timestamp,
    finished_at       timestamp,
    CONSTRAINT ck_counts CHECK (passed >= 0 AND failed >= 0 AND total = passed + failed),
    CONSTRAINT ck_terminal CHECK (
        (status = 'COMPLETED' AND result IS NOT NULL AND finished_at IS NOT NULL)
        OR (status <> 'COMPLETED' AND result IS NULL))
);

CREATE UNIQUE INDEX uq_verification_active ON verifications (idempotency_key)
    WHERE status IN ('QUEUED','RUNNING');
CREATE INDEX ix_verification_http_retry ON verifications (idempotency_key, created_at DESC);
CREATE INDEX ix_verification_submission ON verifications (submission_id, created_at DESC);
CREATE INDEX ix_verification_queue ON verifications (status, created_at) WHERE status = 'QUEUED';

CREATE TABLE test_results (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    verification_id uuid NOT NULL REFERENCES verifications(id) ON DELETE CASCADE,
    ordinal         integer NOT NULL,
    test_name       text    NOT NULL,
    outcome         varchar(4) NOT NULL CHECK (outcome IN ('PASS','FAIL')),
    duration_ms     bigint  NOT NULL DEFAULT 0,
    logs            text    NOT NULL DEFAULT '',
    UNIQUE (verification_id, ordinal)
);

CREATE TABLE processed_messages (
    consumer        text   NOT NULL,
    message_id      text   NOT NULL,
    payload_sha256  varchar(64),
    handled_at      timestamp NOT NULL DEFAULT now(),
    PRIMARY KEY (consumer, message_id)
);

CREATE TABLE ai_summaries (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id uuid        NOT NULL,
    summary       text        NOT NULL,
    model         text        NOT NULL,
    prompt_sha256 varchar(64),
    generated_at  timestamp   NOT NULL DEFAULT now()
);
