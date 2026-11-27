# Postman collections (all six microservices)

Git-native v3 collections kept beside the code. No Postman account or
`POSTMAN_API_KEY` is needed to lint or run them locally.

| Collection | Source | CI gate |
| --- | --- | --- |
| MS1 Challenges | `contracts/ms1-challenges.openapi.yaml` + `Backend/ms1-challenges` | lint + live run |
| MS2 Participants | real `/api/users` endpoints (see drift note) | lint + live run |
| MS3 Soumissions | `contracts/ms3-submissions.openapi.yaml` | lint + live run |
| MS4 Evaluation | real `/experts/*` endpoints (see drift note) | lint + live run (MySQL required) |
| MS5 Verification | `contracts/ms5-verification.openapi.yaml` | lint + live run |
| MS6 Reputation | `contracts/ms6-reputation.openapi.yaml` | lint + live run |

## Run locally

```bash
scripts/test-postman.sh --all       # lint all six collections

scripts/start-all.sh                # starts MS1 (8081), MS2 (8082), MS3 (8083),
                                    # MS4 (8084), MS5 (8085), MS6 (8086)

postman collection run "postman/collections/MS1 Challenges" --delay-request 300
postman collection run "postman/collections/MS2 Participants" --delay-request 300
postman collection run "postman/collections/MS3 Soumissions" --delay-request 500
postman collection run "postman/collections/MS4 Evaluation" --delay-request 500
postman collection run "postman/collections/MS5 Verification" --delay-request 3000
postman collection run "postman/collections/MS6 Reputation" --delay-request 500
```

`scripts/test-postman.sh` also accepts `--ms1` … `--ms6` to lint a single
collection. MS1 requests are kept flat (no folders) because the create request
must run before the requests that use its captured `challengeId`.

MS4 needs MySQL on `localhost:3306` (database `forge_evaluation`, user `root`,
empty password). `scripts/start-all.sh` starts MySQL via Docker when available
and adds the MariaDB dialect override automatically if the local server is
MariaDB. In CI a real MySQL 8 service container is used instead.

MS6 uses the fixed developer fixture from `DataInitializer`
(`11111111-1111-1111-1111-111111111111`). The event-ingest request generates
a fresh challenge UUID in a `beforeRequest` script, so repeated runs do not
hit `DUPLICATE_IGNORED`.

## What the assertions check

- MS1: create → DRAFT, list, detail, update, publish → PUBLISHED, acceptance
  criteria and prizes round-trips. Runtime IDs are captured from responses via
  `pm.collectionVariables.set`.
- MS2: create user → 201 + id, list, detail, update, teams, instance smoke
  check, delete → 204.
- MS3: create → DRAFT, list, detail, submit → SUBMITTED, store verdict → PASS,
  read verdict → fields match.
- MS4: create expert → id returned, detail, list, update, delete.
- MS5: launch → 202 + id, worker → COMPLETED/PASS, redacted logs → 200,
  similarity analyze + stored report → 200, AI summary → 503.
- MS6: seeded developer reputation score ≥ 120, badges, filtered ranking, and
  `SUBMISSION_PASSED` event ingestion → 202 `APPLIED`.

### Known contract drift

- MS1: the collection asserts a paginated `PageResponse` object for
  `GET /api/challenges`, while the frozen contract declares a bare array.
- MS2: `contracts/ms2-participants.openapi.yaml` defines `/api/developers`,
  `/api/teams` and `/api/challenges/{id}/applications`, but the implementation
  exposes `/api/users` CRUD plus `/api/users/{id}/teams`. The collection
  targets the real endpoints; it does **not** validate the frozen MS2 contract.
- MS3: the frozen contract does not declare `commitSha` on
  `POST /api/submissions/{id}/submit`, but the implementation requires it. The
  collection sends it and the request description records the drift.
- MS4: `contracts/ms4-evaluation.openapi.yaml` defines `/api/experts`, jury,
  rubric, evaluation, leaderboard and winners endpoints, while the
  implementation only exposes expert CRUD under `/experts/*`. The collection
  targets the real endpoints; it does **not** validate the frozen MS4 contract.

## CI

`.github/workflows/postman-api-tests.yml` lints all six collections, starts all
six services against a MySQL service, runs all six collections on push/PR,
uploads YAML run reports, and prints service logs on failure. Failed `pm.test`
assertions and failed lints fail the job.
