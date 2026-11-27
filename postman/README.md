# Postman collections (all six microservices)

Git-native v3 collections kept beside the code. No Postman account or
`POSTMAN_API_KEY` is needed to lint or run them locally.

| Collection | Source | CI gate |
| --- | --- | --- |
| MS1 Challenges | `contracts/ms1-challenges.openapi.yaml` | lint only — no implementation in this repo |
| MS2 Participants | `contracts/ms2-participants.openapi.yaml` | lint only — no implementation in this repo |
| MS3 Soumissions | `contracts/ms3-submissions.openapi.yaml` | lint + live run |
| MS4 Evaluation | real `/experts/*` endpoints (see drift note) | lint + live run (MySQL required) |
| MS5 Verification | `contracts/ms5-verification.openapi.yaml` | lint + live run |
| MS6 Reputation | `contracts/ms6-reputation.openapi.yaml` | lint + live run |

## Run locally

```bash
scripts/test-postman.sh --all       # lint all six collections

scripts/start-all.sh                # starts MS3 (8083), MS4 (8084), MS5 (8085), MS6 (8086)

postman collection run "postman/collections/MS3 Soumissions" --delay-request 500
postman collection run "postman/collections/MS4 Evaluation" --delay-request 500
postman collection run "postman/collections/MS5 Verification" --delay-request 3000
postman collection run "postman/collections/MS6 Reputation" --delay-request 500
```

`scripts/test-postman.sh` also accepts `--ms1` … `--ms6` to lint a single
collection. MS1/MS2 are contract-only: their collections describe the intended
API but cannot be run against an implementation in this repository.

MS4 needs MySQL on `localhost:3306` (database `forge_evaluation`, user `root`,
empty password). `scripts/start-all.sh` starts MySQL via Docker when available
and adds the MariaDB dialect override automatically if the local server is
MariaDB. In CI a real MySQL 8 service container is used instead.

MS6 uses the fixed developer fixture from `DataInitializer`
(`11111111-1111-1111-1111-111111111111`). The event-ingest request generates
a fresh challenge UUID in a `beforeRequest` script, so repeated runs do not
hit `DUPLICATE_IGNORED`.

## What the assertions check

- MS1: draft → update → publish, catalogue search, acceptance criteria and
  prizes round-trips (contract only).
- MS2: developer and team creation/read, challenge application round-trip
  (contract only).
- MS3: create → DRAFT, list, detail, submit → SUBMITTED, store verdict → PASS,
  read verdict → fields match. Runtime IDs are captured from responses via
  `pm.collectionVariables.set`.
- MS4: create expert → id returned, detail, list, update, delete. These are the
  endpoints the code actually exposes.
- MS5: launch → 202 + id, worker → COMPLETED/PASS, redacted logs → 200,
  similarity analyze + stored report → 200, AI summary → 503.
- MS6: seeded developer reputation score ≥ 120, badges, filtered ranking, and
  `SUBMISSION_PASSED` event ingestion → 202 `APPLIED`.

### Known contract drift

- MS3: the frozen contract does not declare `commitSha` on
  `POST /api/submissions/{id}/submit`, but the implementation requires it. The
  collection sends it and the request description records the drift.
- MS4: `contracts/ms4-evaluation.openapi.yaml` defines `/api/experts`, jury,
  rubric, evaluation, leaderboard and winners endpoints, while the
  implementation only exposes expert CRUD under `/experts/*`. The collection
  targets the real endpoints; it does **not** validate the frozen MS4 contract.

## CI

`.github/workflows/postman-api-tests.yml` lints all six collections, starts
MS3/MS4/MS5/MS6 against a MySQL service, runs the four live collections on
push/PR, uploads YAML run reports, and prints service logs on failure. Failed
`pm.test` assertions and failed lints fail the job. MS1/MS2 stay lint-only.
