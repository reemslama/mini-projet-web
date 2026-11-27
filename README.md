# Arène — Plateforme de hackathons & bounties

Module **Applications Web Distribuées · MT-41**.

## Sujet

**Arène** est une plateforme de hackathons et de challenges à bounties. Une entreprise publie un
défi (cahier des charges, tests d’acceptation, prime) ; des développeurs soumettent un dépôt GitHub ;
un moteur de vérification exécute les tests ; les experts notent et désignent les gagnants.

## Architecture

6 microservices + infrastructure partagée :

- **MS1 · Challenges** (M1) — publication de défis, cycles de vie, primes. ->  ## Chaima dahdouh
- **MS2 · Participants** (M2) — développeurs, équipes, candidatures. -> ## Naima Hadwej
- **MS3 · Soumissions** (M3) — dépôt GitHub, soumission, verdict. -> ## Amine ichaoui
- **MS4 · Évaluation** (M4) — jury, grilles, scores, classement, gagnants. -> ## Rim slama
- **MS5 · Vérification** (équipe) — technologie avancée : bac à sable, anti-plagiat, IA d’appoint. 
- **MS6 · Réputation** (M5) — profils, badges, classements. -> ## Jihed ramedi

Infrastructure partagée : API Gateway, Keycloak, Eureka, Docker / Compose, Frontend Angular 21.

## Stack

Java 21 + Spring Boot, Spring Cloud Gateway, Eureka, Keycloak (OIDC / JWT), OpenFeign, RabbitMQ
(Spring Cloud Stream), H2 / MySQL / PostgreSQL + pgvector, Docker / Compose, Angular 21 (Node 22).

## Équipe

| Membre | Responsabilité |
| --- | --- |
| Chaima Dahdouh | MS1 · Challenges |
| Naima Hadwej | MS2 · Participants |
| Amine Ichaoui | MS3 · Soumissions |
| Rim Slama | MS4 · Évaluation |
| Jihed Ramedi | MS6 · Réputation |
| Toute l'équipe | MS5 · Vérification, Eureka, Gateway, Keycloak, Docker, Frontend |

## Découverte de services (Eureka) — 1 serveur

| Composant | Port(s) | Nom dans Eureka |
| --- | --- | --- |
| Eureka Server | 8761 (`localhost`) | EUREKA-SERVER |
| MS3 Soumissions | 8083 | MS3-SOUMISSIONS |
| MS4 Évaluation (x2) | 8084 / 8087 | MS4-EVALUATION |
| MS5 Vérification | 8085 | MS5-VERIFICATION |
| MS6 Réputation (x2) | 8086 / 8096 | MS6-REPUTATION |

- Le registre est **unique** : pas de réplication peer-to-peer, donc plus de second dashboard ni de
  doublon `DOWN` sur un replica.
- Chaque client déclare une seule zone :
  `eureka.client.service-url.defaultZone=http://localhost:8761/eureka/`
- Les MS les plus sollicités (MS4 et MS6) tournent en 2 instances sur des ports différents.

### Lancement

```bash
scripts/start-all.sh        # Linux / macOS
scripts/start-all.bat       # Windows
```

Ou dans IntelliJ : `EurekaServerApplication` avec *Active profiles* = `peer1` (profil standalone),
puis chaque microservice (2e instance : *VM options* `-Dserver.port=8096`).

Dashboard : http://localhost:8761

### Captures

![Dashboard Eureka](docs/screenshots/eureka-peer1.png)

Actuator (MS6) :

![health](docs/screenshots/actuator-health.png)
![info](docs/screenshots/actuator-info.png)
![metrics](docs/screenshots/actuator-metrics.png)

## Documentation

- `docs/Document-technique.md` — document technique.
- `docs/Presentation/` — présentation S4 (React + reveal.js).
- `docs/Diagrams/` — diagrammes PlantUML (SVG).
- `contracts/` — contrats OpenAPI par microservice.

## Rapport

Validation S4 : conception, contrats d’API, communication, sécurité, données.
