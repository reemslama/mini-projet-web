# Backend — Arène

Microservices du projet Arène (plateforme de hackathons & bounties).

- `docs/Diagrams/` — diagrammes (cas d’utilisation, séquences, classes).
- `docs/Document-technique.md` — document technique.
- `docs/Presentation/` — présentation.
- `contracts/` — contrats OpenAPI.

Détail par microservice dans le document technique et les diagrammes associés.

## Supervision (Spring Boot Actuator)

Tous les services backend exposent les endpoints `health`, `info` et `metrics` :

| Service | Port | Endpoint de santé |
| --- | --- | --- |
| Eureka peer1 | 8761 | `http://localhost:8761/actuator/health` |
| Eureka peer2 | 8762 | `http://localhost:8762/actuator/health` |
| MS3 Soumission | 8083 | `http://localhost:8083/actuator/health` |
| MS4 Évaluation (x2) | 8084 / 8087 | `http://localhost:8084/actuator/health` |
| MS5 Vérification | 8085 | `http://localhost:8085/actuator/health` |
| MS6 Réputation (x2) | 8086 / 8096 | `http://localhost:8086/actuator/health` |

Pour chaque port, remplacer `health` par `info` ou `metrics` (ex. `http://localhost:8083/actuator/metrics/http.server.requests`).
Le `health` inclut le détail des composants (base de données, disque, Eureka) et les clients
Eureka publient leur état dans le registre via `eureka.client.healthcheck.enabled=true`.
Au démarrage, l'instance peut apparaître `DOWN` dans Eureka pendant le premier battement
(~30 s), le temps que le client se connecte au registre, puis repasse automatiquement `UP`.

## Documentation API (Swagger UI)

Les microservices exposent une documentation interactive générée par springdoc :

| Service | Swagger UI | Spécification JSON |
| --- | --- | --- |
| MS3 Soumission | `http://localhost:8083/swagger-ui.html` | `http://localhost:8083/v3/api-docs` |
| MS4 Évaluation | `http://localhost:8084/swagger-ui.html` | `http://localhost:8084/v3/api-docs` |
| MS5 Vérification | `http://localhost:8085/swagger-ui.html` | `http://localhost:8085/v3/api-docs` |
| MS6 Réputation | `http://localhost:8086/swagger-ui.html` | `http://localhost:8086/v3/api-docs` |

MS6 expose aussi `loggers` (`/actuator/loggers/tn.arene.reputation`) et un HealthIndicator
personnalisé `reputationStore` visible dans `/actuator/health`. Requêtes de test :
`Backend/ms6-reputation/reputation.http`.

> [!note] Référence contractuelle
> Les contrats OpenAPI gelés dans [`contracts/`](../contracts/) restent la source de vérité.
> La documentation générée décrit le code déployé et ne remplace pas ces contrats.
