# Arène — Plateforme de hackathons & bounties

Module **Applications Web Distribuées · MT-41**.

## Sujet

**Arène** est une plateforme de hackathons et de challenges à bounties. Une entreprise publie un
défi (cahier des charges, tests d’acceptation, prime) ; des développeurs soumettent un dépôt GitHub ;
un moteur de vérification exécute les tests ; les experts notent et désignent les gagnants.

## Architecture

6 microservices + infrastructure partagée :

- **MS1 · Challenges** (M1) — publication de défis, cycles de vie, primes.
- **MS2 · Participants** (M2) — développeurs, équipes, candidatures.
- **MS3 · Soumissions** (M3) — dépôt GitHub, soumission, verdict.
- **MS4 · Évaluation** (M4) — jury, grilles, scores, classement, gagnants.
- **MS5 · Vérification** (équipe) — technologie avancée : bac à sable, anti-plagiat, IA d’appoint.
- **MS6 · Réputation** (M5) — profils, badges, classements.

Infrastructure partagée : API Gateway, Keycloak, Eureka, Docker / Compose, Frontend Angular 21.

## Stack

Java 21 + Spring Boot, Spring Cloud Gateway, Eureka, Keycloak (OIDC / JWT), OpenFeign, RabbitMQ
(Spring Cloud Stream), H2 / MySQL / PostgreSQL + pgvector, Docker / Compose, Angular 21 (Node 22).

## Documentation

- `docs/Document-technique.md` — document technique.
- `docs/Presentation/` — présentation S4 (React + reveal.js).
- `docs/Diagrams/` — diagrammes PlantUML (SVG).
- `contracts/` — contrats OpenAPI par microservice.

## Rapport

Validation S4 : conception, contrats d’API, communication, sécurité, données.
