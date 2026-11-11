# Document technique — Arène

Plateforme de hackathons et de bounties — module **Applications Web Distribuées · MT-41**.

Ce document reprend les parties de la presentation de validation (seance 4) avec davantage de
details. Il accompagne le deck (`docs/Presentation/`), les diagrammes (`docs/Diagrams/`) et les
contrats (`contracts/`).

## 1. Sujet et contexte

Une entreprise a un besoin logiciel ponctuel : un logiciel sur mesure, sans recruter d equipe, sans
faire appel a des freelances au forfait. Arène met en place un cadre de challenge : l’entreprise
publie un defi avec cahier des charges, tests d’acceptation et prime ; les developpeurs soumettent
un depot GitHub ; un moteur de verification execute les tests ; les experts notent et designent les
gagnants.

Valeur metier : reduire le coût d’accès à du logiciel sur mesure et offrir aux developpeurs des
projets réels, vérifiés et valorisants.

## 2. Acteurs et fonctionnalités

Acteurs :
- **Entreprise** : publie un defi, fixe le budget, les dates, les tests et la prime.
- **Developpeur / equipe** : s’inscrit, soumet un depot, repond aux tests, est note.
- **Expert** : juge les soumissions, attribue les scores.
- **Admin** : moderation, litiges, comptes.

Fonctionnalités principales :
- Publication de defis (spec, critères, dates, cycle de vie, budget, primes).
- Inscription et candidatures.
- Depot GitHub avec commit figé, controle de la date limite.
- Verification : tests en bac a sable, anti-plagiat, IA d’appoint.
- Notation, classement, designation des gagnants.
- Securisation : authentification via Keycloak, autorisations par roles.

## 3. Découpage en microservices

| Service | Responsabilité | Base de données |
|---|---|---|
| MS1 · Challenges | Defis, spec, critères d’acceptation, primes, catalogue | H2 |
| MS2 · Participants | Developpeurs, equipes, candidatures | H2 |
| MS3 · Soumissions | Depot, soumission, verdict, date limite | H2 |
| MS4 · Évaluation | Jury, grilles pondérées, scores, classement, gagnants | MySQL |
| MS5 · Vérification | Bac à sable, anti-plagiat, IA d’appoint | PostgreSQL + pgvector |
| MS6 · Réputation | Profils, badges, classements | H2 |

## 4. Données gérées par service

Une base par service, aucune cle étrangère inter-services. Les references croisees passent par
identifiant et par evenements.

## 5. Interactions entre services

- Synchrone : OpenFeign via l’API Gateway, decouverte via Eureka (appels service a service).
- Asynchrone : RabbitMQ via Spring Cloud Stream (flux evenementiel).
- Sécurité : JWT valide via Keycloak ; chaque service valide le jeton.

## 6. Diagramme d’architecture globale

Voir `docs/Diagrams/` (diagramme global) et le deck (`docs/Presentation/` slide 7).

## 7. Points de la presentation, avec details

### 7.1 Idee et besoin
Le probleme : un besoin logiciel ponctuel, sans equipe interne ni freelance. La solution : un
challenge verifie. Les acteurs et cas d’utilisation sont definis.

### 7.2 Utilisateurs
Les quatre roles (entreprise, developpeur/equipe, expert, admin) correspondent aux besoins
metiers identifies.

### 7.3 Scénario de bout en bout
Publication → inscription → depot → verification → notation → gagnants.

### 7.4 Périmètre MoSCoW
Must : publication, inscription, depot, tests, verdict, notation, classement, auth, Gateway,
Eureka. Should / Could / Won’t.

### 7.5 Architecture globale
Diagramme, services, Gateway, securite, donnees, communications.

### 7.6 Microservices
Chaque service est autonome, avec sa base et ses responsabilites.

### 7.7 Strategie de donnees
Une base par service.

### 7.8 Communication synchrone
OpenFeign via Gateway, Eureka pour la decouverte.

### 7.9 Communication asynchrone
RabbitMQ, flux evenementiel.

### 7.10 Technologie avancée MS5
Bac a sable, anti-plagiat (pgvector), IA d’appoint.

### 7.11 Gateway, securite et configuration
Keycloak en infrastructure, pas de microservice d’auth.

### 7.12 Contrats d’API
39 endpoints repartis sur les six microservices, gelés en S4. Specs OpenAPI 3.0.3 dans
`contracts/` (`ms1-challenges.openapi.yaml` … `ms6-reputation.openapi.yaml`). Conventions :
base path `/api`, JSON, `Authorization: Bearer <JWT>`, enveloppe `ErrorResponse`, pagination
`Page<T>`. Catalogue d’evenements RabbitMQ et `docker-compose.yml` : voir le document
source `Arène-contrats-et-deploiement.md`.

### 7.13 Stack technique
Java, Spring, Gateway, Eureka, Keycloak, OpenFeign, RabbitMQ, bases, Docker, Angular.

### 7.14 Répartition des tâches
M1 a MS1, M2 a MS2, M3 a MS3, M4 a MS4, M5 a MS6, equipe pour MS5 + infra + front.

### 7.15 Pilotage et organisation Git
GitHub Projects, milestones, epices, Git branching.

### 7.16 Planning et risques
Roadmap S4 a S10, risques et mitigations.

## 8. Couche IA / LLM (amélioration optionnelle)

Le coeur de la plateforme fonctionne **sans IA**. La couche LLM est une amélioration optionnelle
dans le service avancé (MS5 · Vérification) :

| Capacité | Service | Usage | Validation |
|---|---|---|---|
| Résumé de soumission pour le jury | MS5 (LLM) | lit la soumission (MS3), transmis au jury (MS4) | Oui |
| Rédaction de tests d’acceptation | MS5 (LLM) | génère un draft de spec de tests → **humain validé** | Oui |
| Matching expert ↔ défi | MS5 (embeddings k-NN + LLM) | lit MS2 (experts) + MS1 (défis) | Oui |
| Détection conflit d’intérêt | MS5 (embeddings) | lit MS2 + MS4 | Oui |

Ligne de defense : « Nous utilisons le LLM là où il ajoute de la valeur (résumés, rédaction de
tests), et des algorithmes deterministes ailleurs (execution des tests, similarité). Le coeur de la
plateforme ne depend jamais de l’IA. »

Dechet : `docker compose stop ms5-verification` → la plateforme reste utilisable, la verification
reste manuelle.

## 9. Conclusion

Conception S4 validee : architecture coherente, contrats definis, communication prete, securite
prete, donnees prete.
