# Contrats d'API — Arène

Contrats OpenAPI 3.0.3 **gelés en S4** pour les six microservices. Chaque membre peut
développer son service indépendamment (avec des mocks) avant l'intégration S5–S9.

| Fichier | Service | Port |
|---|---|---|
| `ms1-challenges.openapi.yaml` | MS1 · Challenges | 8081 |
| `ms2-participants.openapi.yaml` | MS2 · Participants | 8082 |
| `ms3-submissions.openapi.yaml` | MS3 · Soumissions | 8083 |
| `ms4-evaluation.openapi.yaml` | MS4 · Évaluation | 8084 |
| `ms5-verification.openapi.yaml` | MS5 · Vérification (avancé) | 8085 |
| `ms6-reputation.openapi.yaml` | MS6 · Réputation | 8086 |

Total : **39 endpoints** (34 lignes groupées GET·PUT / POST·GET).

Conventions transverses : base path `/api`, JSON, authentification `Authorization: Bearer <JWT>`
(Keycloak), enveloppe d'erreur `ErrorResponse`, pagination `Page<T>`. Voir
`docs/Document-technique.md` § 7.12 et le deck (`docs/Presentation/`).

Le catalogue d'événements RabbitMQ et le `docker-compose.yml` sont dans le document source
`Arène-contrats-et-deploiement.md` (hors dépôt).