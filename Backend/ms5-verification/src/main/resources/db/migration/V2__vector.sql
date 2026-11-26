CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE code_embeddings (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id  uuid        NOT NULL,
    challenge_id   uuid        NOT NULL,
    chunk_id       text        NOT NULL,
    source_path    text,
    content_sha256 varchar(64),
    model_version  varchar(96) NOT NULL,
    vector_text    text        NOT NULL,
    vector         vector(384),
    created_at     timestamp   NOT NULL DEFAULT now(),
    UNIQUE (submission_id, chunk_id, model_version)
);
CREATE INDEX ix_embeddings_scope ON code_embeddings (challenge_id, submission_id, model_version);
CREATE INDEX ix_embeddings_vector ON code_embeddings USING hnsw (vector vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

CREATE TABLE similarity_reports (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id  uuid NOT NULL,
    challenge_id   uuid NOT NULL,
    max_similarity double precision NOT NULL CHECK (max_similarity BETWEEN 0 AND 1),
    flagged        boolean NOT NULL,
    threshold      double precision NOT NULL,
    model_version  varchar(96) NOT NULL,
    analyzed_at    timestamp NOT NULL DEFAULT now()
);
CREATE INDEX ix_similarity_lookup ON similarity_reports (submission_id, analyzed_at DESC);

CREATE TABLE similarity_matches (
    report_id           uuid NOT NULL REFERENCES similarity_reports(id) ON DELETE CASCADE,
    other_submission_id uuid NOT NULL,
    similarity          double precision NOT NULL CHECK (similarity BETWEEN 0 AND 1),
    PRIMARY KEY (report_id, other_submission_id)
);
