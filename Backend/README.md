# Backend — Arène

Microservices du projet Arène (plateforme de hackathons & bounties).

- `docs/Diagrams/` — diagrammes (cas d’utilisation, séquences, classes).
- `docs/Document-technique.md` — document technique.
- `docs/Presentation/` — présentation.
- `contracts/` — contrats OpenAPI.

Détail par microservice dans le document technique et les diagrammes associés.

## Supervision (Spring Boot Actuator)

Tous les services backend exposent les endpoints `health`, `info`, `metrics` et `loggers` :

| Service | Port | Endpoint de santé | Endpoint Info |
| --- | --- | --- | --- |
| Eureka Server | 8761 | `http://localhost:8761/actuator/health` | `http://localhost:8761/actuator/info` |
| MS1 Challenges | 8081 | `http://localhost:8081/actuator/health` | `http://localhost:8081/actuator/info` |
| MS3 Soumission | 8083 | `http://localhost:8083/actuator/health` | `http://localhost:8083/actuator/info` |
| MS4 Évaluation (x2) | 8084 / 8085 | `http://localhost:8084/actuator/health` | `http://localhost:8084/actuator/info` |

### Fonctionnalités Actuator & Observabilité

1. **Santé détaillée (`/actuator/health`)** :
   * Inclut l'état des composants internes : base H2 (`db`), espace disque (`diskSpace`), ping, et le `HealthIndicator` personnalisé `challengeCustomHealth`.
   * Grâce à `eureka.client.healthcheck.enabled=true`, toute défaillance d'un composant propage le statut `DOWN` directement dans le dashboard Eureka (http://localhost:8761).

2. **Simulation de panne et transition UP ↔ DOWN (Atelier)** :
   * MS1 intègre un commutateur de test pour simuler une panne sans arrêter le processus :
     * Déclencher la panne : `POST http://localhost:8081/api/challenges/actuator/simulate-down`
     * Observer le passage à `DOWN` sur `/actuator/health` (code HTTP 503) puis dans le dashboard Eureka.
     * Rétablir le service : `POST http://localhost:8081/api/challenges/actuator/simulate-up` (repasse à `UP`).

3. **Informations du service (`/actuator/info`)** :
   * Expose le nom, la version, la description et le référent d'équipe (`Chaima Dahdouh`).
   * Le clic sur l'instance dans Eureka redirige automatiquement vers `/actuator/info`.

4. **Métriques de performance (`/actuator/metrics`)** :
   * Mémoire utilisée : `GET http://localhost:8081/actuator/metrics/jvm.memory.used`
   * Requêtes HTTP traitées : `GET http://localhost:8081/actuator/metrics/http.server.requests` (mesures `COUNT` et `MAX`).
   * Filtrage par code de statut : `GET http://localhost:8081/actuator/metrics/http.server.requests?tag=status:200`

5. **Modification dynamique des logs (`/actuator/loggers`)** :
   * Consulter le niveau d'un logger : `GET http://localhost:8081/actuator/loggers/tn.arene.ms1challenges`
   * Modifier le niveau à chaud (ex: passer en DEBUG sans redémarrer) :
     ```http
     POST http://localhost:8081/actuator/loggers/tn.arene.ms1challenges
     Content-Type: application/json

     { "configuredLevel": "DEBUG" }
     ```

6. **Services Polyglottes Node.js et Python (Partie 5)** :
   * **Node.js (Express)** : expose `GET /health` renvoyant `{"status": "UP"}` et `GET /info`, configurés dans `eureka-js-client` via `healthCheckUrl: "http://localhost:8083/health"` et `statusPageUrl: "http://localhost:8083/info"`.
   * **Python (FastAPI)** : expose `@app.get("/health")` et `@app.get("/info")`, configurés dans `py-eureka-client` via `health_check_url="http://localhost:8084/health"` et `status_page_url="http://localhost:8084/info"`.

## Documentation API (Swagger UI)

Les microservices exposent une documentation interactive générée par springdoc :

| Service | Swagger UI | Spécification JSON |
| --- | --- | --- |
| MS1 Challenges | `http://localhost:8081/swagger-ui.html` | `http://localhost:8081/v3/api-docs` |
| MS3 Soumission | `http://localhost:8083/swagger-ui.html` | `http://localhost:8083/v3/api-docs` |
| MS4 Évaluation | `http://localhost:8084/swagger-ui.html` | `http://localhost:8084/v3/api-docs` |

> [!note] Référence contractuelle
> Les contrats OpenAPI gelés dans [`contracts/`](../contracts/) restent la source de vérité.
> La documentation générée décrit le code déployé et ne remplace pas ces contrats.
