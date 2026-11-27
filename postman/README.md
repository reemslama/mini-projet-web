# Postman collections (MS3 Soumissions + MS5 Vérification)

Two Git-native v3 collections generated from `contracts/ms3-submissions.openapi.yaml`
and `contracts/ms5-verification.openapi.yaml`, then curated with deterministic
inputs and `pm.test` assertions. No Postman account is needed to run them locally.

## Run locally

```bash
scripts/start-all.sh --skip-db    # only MS3 (8083) and MS5 (8085) are used here

postman collection lint "postman/collections/MS3 Soumissions" -f error
postman collection lint "postman/collections/MS5 Verification" -f error

postman collection run "postman/collections/MS3 Soumissions" --delay-request 500
postman collection run "postman/collections/MS5 Verification" --delay-request 3000
```

MS5 needs `--delay-request 3000` because verification is asynchronous: the
worker starts after ~2s, so an immediate `GET /api/verifications/{id}/logs`
returns 404. AI summarization is expected to return 503 in the local dev
configuration (`ms5.ai.enabled=false`).

## What the assertions check

- MS3: create → DRAFT, list, detail, submit → SUBMITTED, store verdict → PASS,
  read verdict → fields match. Runtime IDs are captured from responses via
  `pm.collectionVariables.set`.
- MS5: launch → 202 + id, worker → COMPLETED/PASS, redacted logs → 200,
  similarity analyze + stored report → 200, AI summary → 503.

Note: the frozen MS3 contract does not declare `commitSha` on
`POST /api/submissions/{id}/submit`, but the implementation requires it. The
collection sends it and the request description records the drift.

## CI

`.github/workflows/postman-api-tests.yml` runs the lint gates and both
collections on push/PR with real services, uploads YAML run reports, and
preserves nonzero exits as build failures.
