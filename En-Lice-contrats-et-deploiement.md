---
title: En Lice — Contrats d'API, catalogue d'événements & déploiement
tags:
  - mini-projet
  - awd
  - s4
  - conception
  - api
  - openapi
  - evenements
  - docker
aliases:
  - En Lice Contrats
  - En Lice Livrables techniques
status: draft
---

# En Lice — Contrats d'API, catalogue d'événements & déploiement

> [!info] Objet du document
> Ce document complète le [[requirements|dossier de conception S4]] et la [[En-Lice-FR|description du projet]] avec les **livrables techniques gelés en S4** :
> 1. les **contrats OpenAPI** des six microservices,
> 2. le **catalogue d'événements** RabbitMQ (Spring Cloud Stream),
> 3. le **`docker-compose.yml`** de bout en bout.
>
> Les contrats sont **gelés en S4** : ils permettent à chaque membre de développer son service indépendamment (avec des mocks) avant l'intégration S5–S9.

---

## 1. Conventions transverses

| Sujet | Convention |
|---|---|
| Base path | `/api` (la Gateway route `/{service}/api/**` → `/{service}/api/**` du service) |
| Format | JSON (`application/json`) |
| Authentification | `Authorization: Bearer <JWT>` — jeton Keycloak, validé par chaque service |
| Erreurs | Enveloppe unique `ErrorResponse` (voir ci-dessous) |
| Pagination | `page` (0-based), `size`, `sort` — réponse `Page<T>` (`content`, `totalElements`, `totalPages`, `number`, `size`) |
| Versionnage | Par l'URL si rupture : `/api/v2/...` |
| Docs | `springdoc-openapi` → `/swagger-ui.html` et `/v3/api-docs` sur chaque service |

**Enveloppe d'erreur (`ErrorResponse`) :**

```json
{
  "timestamp": "2026-11-11T10:15:30Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Le champ 'title' est obligatoire",
  "path": "/api/challenges"
}
```

> [!warning] Auth = Keycloak (infrastructure), **pas** un microservice
> Keycloak **est** le service d'identité : on le **configure** (realm `enlice`, rôles, clients), on ne le code pas. Chaque service se contente de **valider le JWT** (`SecurityFilterChain`, `@PreAuthorize`).

**Rôles Keycloak :** `DEVELOPER`, `ENTERPRISE`, `EXPERT`, `ADMIN`.

---

## 2. Contrats OpenAPI

### 2.1 MS1 · Challenges — port `8081`

```yaml
openapi: 3.0.3
info:
  title: En Lice · MS1 Challenges
  version: 1.0.0
  description: Espace entreprise, défis, critères d'acceptation, primes, catalogue.
servers:
  - url: http://localhost:8080/ms1   # via la Gateway
  - url: http://localhost:8081       # accès direct (dev)
tags:
  - name: Challenges
paths:
  /api/challenges:
    post:
      tags: [Challenges]
      summary: Créer un défi (brouillon)
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/ChallengeCreate' }
      responses:
        '201':
          description: Défi créé
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Challenge' }
        '400': { $ref: '#/components/responses/BadRequest' }
        '403': { $ref: '#/components/responses/Forbidden' }
    get:
      tags: [Challenges]
      summary: Catalogue / recherche de défis
      parameters:
        - { name: tag,      in: query, schema: { type: string } }
        - { name: status,   in: query, schema: { $ref: '#/components/schemas/ChallengeStatus' } }
        - { name: minPrize, in: query, schema: { type: number, format: double } }
        - { name: page, in: query, schema: { type: integer, default: 0 } }
        - { name: size, in: query, schema: { type: integer, default: 20 } }
      responses:
        '200':
          description: Page de défis
          content:
            application/json:
              schema:
                type: object
                properties:
                  content: { type: array, items: { $ref: '#/components/schemas/Challenge' } }
                  totalElements: { type: integer }
                  totalPages: { type: integer }
                  number: { type: integer }
                  size: { type: integer }
  /api/challenges/{id}:
    parameters:
      - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
    get:
      tags: [Challenges]
      summary: Détail d'un défi
      responses:
        '200':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Challenge' }
        '404': { $ref: '#/components/responses/NotFound' }
    put:
      tags: [Challenges]
      summary: Mettre à jour un défi (propriétaire, statut DRAFT)
      security: [{ bearerAuth: [] }]
      requestBody:
        content:
          application/json:
            schema: { $ref: '#/components/schemas/ChallengeCreate' }
      responses:
        '200':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Challenge' }
  /api/challenges/{id}/publish:
    post:
      tags: [Challenges]
      summary: Publier un défi (déclenche challenge.published)
      security: [{ bearerAuth: [] }]
      parameters:
        - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
      responses:
        '200':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Challenge' }
        '409': { description: Défi déjà publié ou incomplet }
  /api/challenges/{id}/acceptance-criteria:
    parameters:
      - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
    get:
      tags: [Challenges]
      summary: Lire la spec de tests d'acceptation (appelée par MS5)
      responses:
        '200':
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/AcceptanceCriterion' }
    put:
      tags: [Challenges]
      summary: Définir la spec de tests d'acceptation
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: array
              items: { $ref: '#/components/schemas/AcceptanceCriterion' }
      responses:
        '200':
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/AcceptanceCriterion' }
  /api/challenges/{id}/prizes:
    parameters:
      - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
    get:
      tags: [Challenges]
      summary: Lire les primes (top 3)
      responses:
        '200':
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/Prize' }
    put:
      tags: [Challenges]
      summary: Définir les primes (top 3)
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: array
              items: { $ref: '#/components/schemas/Prize' }
      responses:
        '200':
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/Prize' }
components:
  securitySchemes:
    bearerAuth: { type: http, scheme: bearer, bearerFormat: JWT }
  responses:
    BadRequest:
      description: Requête invalide
      content:
        application/json:
          schema: { $ref: '#/components/schemas/ErrorResponse' }
    Forbidden:
      description: Accès refusé
      content:
        application/json:
          schema: { $ref: '#/components/schemas/ErrorResponse' }
    NotFound:
      description: Ressource introuvable
      content:
        application/json:
          schema: { $ref: '#/components/schemas/ErrorResponse' }
  schemas:
    ChallengeStatus:
      type: string
      enum: [DRAFT, PUBLISHED, IN_PROGRESS, JUDGING, CLOSED, CANCELLED]
    ChallengeCreate:
      type: object
      required: [title, description, enterpriseId, deadline]
      properties:
        title: { type: string, maxLength: 140 }
        description: { type: string }
        enterpriseId: { type: string, format: uuid }
        deadline: { type: string, format: date-time }
        tags: { type: array, items: { type: string } }
    Challenge:
      allOf:
        - $ref: '#/components/schemas/ChallengeCreate'
        - type: object
          properties:
            id: { type: string, format: uuid }
            status: { $ref: '#/components/schemas/ChallengeStatus' }
            createdAt: { type: string, format: date-time }
            publishedAt: { type: string, format: date-time, nullable: true }
    AcceptanceCriterion:
      type: object
      required: [id, description, weight]
      properties:
        id: { type: string }
        description: { type: string }
        weight: { type: number, format: double, minimum: 0, maximum: 1 }
        testCommand: { type: string, description: "Commande exécutée dans le bac à sable (optionnel)" }
    Prize:
      type: object
      required: [rank, amount, currency]
      properties:
        rank: { type: integer, minimum: 1, maximum: 3 }
        amount: { type: number, format: double }
        currency: { type: string, default: EUR }
    ErrorResponse:
      type: object
      properties:
        timestamp: { type: string, format: date-time }
        status: { type: integer }
        error: { type: string }
        message: { type: string }
        path: { type: string }
```

### 2.2 MS2 · Participants — port `8082`

```yaml
openapi: 3.0.3
info:
  title: En Lice · MS2 Participants
  version: 1.0.0
  description: Développeurs, équipes, inscriptions/candidatures aux défis.
servers:
  - url: http://localhost:8080/ms2
  - url: http://localhost:8082
paths:
  /api/developers:
    post:
      tags: [Developers]
      summary: Créer un profil développeur (idempotent sur keycloakId)
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/DeveloperCreate' }
      responses:
        '201':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Developer' }
        '409': { description: Profil déjà existant }
  /api/developers/{id}:
    get:
      tags: [Developers]
      summary: Profil développeur
      parameters:
        - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
      responses:
        '200':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Developer' }
        '404': { description: Introuvable }
  /api/teams:
    post:
      tags: [Teams]
      summary: Créer une équipe
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/TeamCreate' }
      responses:
        '201':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Team' }
  /api/teams/{id}:
    get:
      tags: [Teams]
      summary: Détail d'une équipe
      parameters:
        - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
      responses:
        '200':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Team' }
  /api/challenges/{challengeId}/applications:
    parameters:
      - { name: challengeId, in: path, required: true, schema: { type: string, format: uuid } }
    post:
      tags: [Applications]
      summary: S'inscrire à un défi (déclenche application.registered)
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/ApplicationCreate' }
      responses:
        '201':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Application' }
        '409': { description: Déjà inscrit ou défi clos }
    get:
      tags: [Applications]
      summary: Lister les candidatures d'un défi
      responses:
        '200':
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/Application' }
components:
  securitySchemes:
    bearerAuth: { type: http, scheme: bearer, bearerFormat: JWT }
  schemas:
    DeveloperCreate:
      type: object
      required: [keycloakId, displayName]
      properties:
        keycloakId: { type: string }
        displayName: { type: string }
        email: { type: string, format: email }
        skills: { type: array, items: { type: string } }
    Developer:
      allOf:
        - $ref: '#/components/schemas/DeveloperCreate'
        - type: object
          properties:
            id: { type: string, format: uuid }
            createdAt: { type: string, format: date-time }
    TeamCreate:
      type: object
      required: [name, ownerId]
      properties:
        name: { type: string }
        ownerId: { type: string, format: uuid, description: "developerId du chef d'équipe" }
        memberIds: { type: array, items: { type: string, format: uuid } }
    Team:
      allOf:
        - $ref: '#/components/schemas/TeamCreate'
        - type: object
          properties:
            id: { type: string, format: uuid }
    ApplicationCreate:
      type: object
      required: [applicantType, applicantId]
      properties:
        applicantType: { type: string, enum: [DEVELOPER, TEAM] }
        applicantId: { type: string, format: uuid }
    Application:
      allOf:
        - $ref: '#/components/schemas/ApplicationCreate'
        - type: object
          properties:
            id: { type: string, format: uuid }
            challengeId: { type: string, format: uuid }
            status: { type: string, enum: [PENDING, ACCEPTED, REJECTED, WITHDRAWN] }
            registeredAt: { type: string, format: date-time }
```

### 2.3 MS3 · Soumissions — port `8083`

```yaml
openapi: 3.0.3
info:
  title: En Lice · MS3 Soumissions
  version: 1.0.0
  description: Dépôt GitHub, commit figé, artefacts, cycle de vie, contrôle de date limite.
servers:
  - url: http://localhost:8080/ms3
  - url: http://localhost:8083
paths:
  /api/submissions:
    post:
      tags: [Submissions]
      summary: Créer une soumission (brouillon)
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/SubmissionCreate' }
      responses:
        '201':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Submission' }
    get:
      tags: [Submissions]
      summary: Lister les soumissions d'un défi
      parameters:
        - { name: challengeId, in: query, required: true, schema: { type: string, format: uuid } }
        - { name: status, in: query, schema: { $ref: '#/components/schemas/SubmissionStatus' } }
      responses:
        '200':
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/Submission' }
  /api/submissions/{id}:
    get:
      tags: [Submissions]
      summary: Détail d'une soumission
      parameters:
        - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
      responses:
        '200':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Submission' }
        '404': { description: Introuvable }
  /api/submissions/{id}/submit:
    post:
      tags: [Submissions]
      summary: Soumettre définitivement (fige le commit, vérifie la date limite, émet submission.submitted)
      security: [{ bearerAuth: [] }]
      parameters:
        - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
      responses:
        '200':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Submission' }
        '409': { description: Date limite dépassée ou déjà soumise }
  /api/submissions/{id}/verdict:
    get:
      tags: [Submissions]
      summary: Lire le verdict de vérification
      parameters:
        - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
      responses:
        '200':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Verdict' }
        '404': { description: Pas encore de verdict }
    put:
      tags: [Submissions]
      summary: Enregistrer le verdict (appel interne de MS5)
      security: [{ bearerAuth: [] }]
      parameters:
        - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/Verdict' }
      responses:
        '200':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Verdict' }
components:
  securitySchemes:
    bearerAuth: { type: http, scheme: bearer, bearerFormat: JWT }
  schemas:
    SubmissionStatus:
      type: string
      enum: [DRAFT, SUBMITTED, VERIFYING, PASSED, REJECTED, WITHDRAWN]
    SubmissionCreate:
      type: object
      required: [challengeId, applicantId, repoUrl]
      properties:
        challengeId: { type: string, format: uuid }
        applicantId: { type: string, format: uuid }
        repoUrl: { type: string, format: uri, description: "Dépôt GitHub public" }
        branch: { type: string, default: main }
    Submission:
      allOf:
        - $ref: '#/components/schemas/SubmissionCreate'
        - type: object
          properties:
            id: { type: string, format: uuid }
            status: { $ref: '#/components/schemas/SubmissionStatus' }
            commitSha: { type: string, nullable: true, description: "Commit figé à la soumission" }
            submittedAt: { type: string, format: date-time, nullable: true }
    Verdict:
      type: object
      properties:
        submissionId: { type: string, format: uuid }
        result: { type: string, enum: [PASS, FAIL] }
        passed: { type: integer }
        failed: { type: integer }
        total: { type: integer }
        coverage: { type: number, format: double, nullable: true }
        logsUrl: { type: string, format: uri }
        verifiedAt: { type: string, format: date-time }
```

### 2.4 MS4 · Évaluation — port `8084`

```yaml
openapi: 3.0.3
info:
  title: En Lice · MS4 Évaluation
  version: 1.0.0
  description: Experts, jury, grilles pondérées, scores, leaderboard, désignation des gagnants.
servers:
  - url: http://localhost:8080/ms4
  - url: http://localhost:8084
paths:
  /api/experts:
    post:
      tags: [Experts]
      summary: Créer un profil expert
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/ExpertCreate' }
      responses:
        '201':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Expert' }
  /api/challenges/{challengeId}/jury:
    parameters:
      - { name: challengeId, in: path, required: true, schema: { type: string, format: uuid } }
    post:
      tags: [Jury]
      summary: Constituer le jury d'un défi
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [expertIds]
              properties:
                expertIds: { type: array, items: { type: string, format: uuid } }
      responses:
        '201':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Jury' }
    get:
      tags: [Jury]
      summary: Lire le jury d'un défi
      responses:
        '200':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Jury' }
  /api/rubrics:
    post:
      tags: [Rubrics]
      summary: Créer une grille pondérée
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/RubricCreate' }
      responses:
        '201':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Rubric' }
    get:
      tags: [Rubrics]
      summary: Grilles d'un défi
      parameters:
        - { name: challengeId, in: query, required: true, schema: { type: string, format: uuid } }
      responses:
        '200':
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/Rubric' }
  /api/evaluations:
    post:
      tags: [Evaluations]
      summary: Noter une soumission (experts)
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/EvaluationCreate' }
      responses:
        '201':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Evaluation' }
        '409': { description: Soumission non retenue (verdict != PASS) }
  /api/challenges/{challengeId}/leaderboard:
    get:
      tags: [Leaderboard]
      summary: Classement d'un défi (moyenne pondérée)
      parameters:
        - { name: challengeId, in: path, required: true, schema: { type: string, format: uuid } }
      responses:
        '200':
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/LeaderboardEntry' }
  /api/challenges/{challengeId}/declare-winners:
    post:
      tags: [Winners]
      summary: Désigner les gagnants (top 3) — émet winners.declared
      security: [{ bearerAuth: [] }]
      parameters:
        - { name: challengeId, in: path, required: true, schema: { type: string, format: uuid } }
      responses:
        '200':
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/Winner' }
        '409': { description: Notation incomplète ou défi déjà clôturé }
components:
  securitySchemes:
    bearerAuth: { type: http, scheme: bearer, bearerFormat: JWT }
  schemas:
    ExpertCreate:
      type: object
      required: [keycloakId, displayName]
      properties:
        keycloakId: { type: string }
        displayName: { type: string }
        domains: { type: array, items: { type: string } }
    Expert:
      allOf:
        - $ref: '#/components/schemas/ExpertCreate'
        - type: object
          properties:
            id: { type: string, format: uuid }
    Jury:
      type: object
      properties:
        challengeId: { type: string, format: uuid }
        expertIds: { type: array, items: { type: string, format: uuid } }
    RubricCreate:
      type: object
      required: [challengeId, name, criteria]
      properties:
        challengeId: { type: string, format: uuid }
        name: { type: string }
        criteria:
          type: array
          items:
            type: object
            required: [label, weight, maxScore]
            properties:
              label: { type: string }
              weight: { type: number, format: double }
              maxScore: { type: number, format: double, default: 10 }
    Rubric:
      allOf:
        - $ref: '#/components/schemas/RubricCreate'
        - type: object
          properties:
            id: { type: string, format: uuid }
    EvaluationCreate:
      type: object
      required: [submissionId, expertId, rubricId, scores]
      properties:
        submissionId: { type: string, format: uuid }
        expertId: { type: string, format: uuid }
        rubricId: { type: string, format: uuid }
        scores:
          type: array
          items:
            type: object
            required: [criterionLabel, score]
            properties:
              criterionLabel: { type: string }
              score: { type: number, format: double }
        comment: { type: string }
    Evaluation:
      allOf:
        - $ref: '#/components/schemas/EvaluationCreate'
        - type: object
          properties:
            id: { type: string, format: uuid }
            weightedScore: { type: number, format: double }
            evaluatedAt: { type: string, format: date-time }
    LeaderboardEntry:
      type: object
      properties:
        rank: { type: integer }
        submissionId: { type: string, format: uuid }
        applicantId: { type: string, format: uuid }
        averageScore: { type: number, format: double }
        evaluationsCount: { type: integer }
    Winner:
      type: object
      properties:
        rank: { type: integer }
        submissionId: { type: string, format: uuid }
        applicantId: { type: string, format: uuid }
        score: { type: number, format: double }
        prizeAmount: { type: number, format: double }
```

### 2.5 MS5 · Vérification *(avancé, équipe)* — port `8085`

```yaml
openapi: 3.0.3
info:
  title: En Lice · MS5 Vérification
  version: 1.0.0
  description: >
    Technologie avancée : exécution des tests d'acceptation en bac à sable,
    anti-plagiat par similarité vectorielle (pgvector), IA d'appoint.
servers:
  - url: http://localhost:8080/ms5
  - url: http://localhost:8085
paths:
  /api/verifications:
    post:
      tags: [Verifications]
      summary: Lancer une vérification (manuel ou rejeu)
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/VerificationRequest' }
      responses:
        '202':
          description: Vérification acceptée (asynchrone)
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Verification' }
  /api/verifications/{id}:
    get:
      tags: [Verifications]
      summary: État / résultat d'une vérification
      parameters:
        - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
      responses:
        '200':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Verification' }
        '404': { description: Introuvable }
  /api/verifications/{id}/logs:
    get:
      tags: [Verifications]
      summary: Logs d'exécution (texte brut)
      parameters:
        - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
      responses:
        '200':
          content:
            text/plain:
              schema: { type: string }
  /api/similarity/analyze:
    post:
      tags: [Similarity]
      summary: Analyser la similarité d'une soumission vs les autres (pgvector)
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [submissionId, challengeId]
              properties:
                submissionId: { type: string, format: uuid }
                challengeId: { type: string, format: uuid }
      responses:
        '200':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SimilarityReport' }
  /api/similarity/{submissionId}:
    get:
      tags: [Similarity]
      summary: Rapport de similarité déjà calculé
      parameters:
        - { name: submissionId, in: path, required: true, schema: { type: string, format: uuid } }
      responses:
        '200':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SimilarityReport' }
  /api/ai/summarize/{submissionId}:
    post:
      tags: [AI]
      summary: Résumé IA d'une soumission pour le jury (amélioration, non bloquant)
      security: [{ bearerAuth: [] }]
      parameters:
        - { name: submissionId, in: path, required: true, schema: { type: string, format: uuid } }
      responses:
        '200':
          content:
            application/json:
              schema:
                type: object
                properties:
                  submissionId: { type: string, format: uuid }
                  summary: { type: string }
                  model: { type: string }
                  generatedAt: { type: string, format: date-time }
        '503': { description: Service IA indisponible — la plateforme reste utilisable }
components:
  securitySchemes:
    bearerAuth: { type: http, scheme: bearer, bearerFormat: JWT }
  schemas:
    VerificationRequest:
      type: object
      required: [submissionId]
      properties:
        submissionId: { type: string, format: uuid }
        challengeId: { type: string, format: uuid }
        repoUrl: { type: string, format: uri }
        commitSha: { type: string }
        testCommand: { type: string }
    Verification:
      type: object
      properties:
        id: { type: string, format: uuid }
        submissionId: { type: string, format: uuid }
        status: { type: string, enum: [QUEUED, RUNNING, COMPLETED, ERROR] }
        result: { type: string, enum: [PASS, FAIL], nullable: true }
        passed: { type: integer }
        failed: { type: integer }
        total: { type: integer }
        coverage: { type: number, format: double, nullable: true }
        durationMs: { type: integer }
        sandboxImage: { type: string }
        startedAt: { type: string, format: date-time }
        finishedAt: { type: string, format: date-time, nullable: true }
    SimilarityReport:
      type: object
      properties:
        submissionId: { type: string, format: uuid }
        challengeId: { type: string, format: uuid }
        maxSimilarity: { type: number, format: double, description: "Similarité cosinus 0..1" }
        flagged: { type: boolean }
        matches:
          type: array
          items:
            type: object
            properties:
              otherSubmissionId: { type: string, format: uuid }
              similarity: { type: number, format: double }
        analyzedAt: { type: string, format: date-time }
```

### 2.6 MS6 · Réputation — port `8086`

```yaml
openapi: 3.0.3
info:
  title: En Lice · MS6 Réputation
  version: 1.0.0
  description: Profils, réalisations vérifiées, compétences, classements.
servers:
  - url: http://localhost:8080/ms6
  - url: http://localhost:8086
paths:
  /api/developers/{id}/reputation:
    get:
      tags: [Reputation]
      summary: Profil de réputation d'un développeur
      parameters:
        - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
      responses:
        '200':
          content:
            application/json:
              schema: { $ref: '#/components/schemas/ReputationProfile' }
        '404': { description: Introuvable }
  /api/developers/{id}/badges:
    get:
      tags: [Reputation]
      summary: Badges obtenus
      parameters:
        - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
      responses:
        '200':
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/Badge' }
  /api/rankings:
    get:
      tags: [Rankings]
      summary: Classement global, filtrable par compétence
      parameters:
        - { name: skill, in: query, schema: { type: string } }
        - { name: page, in: query, schema: { type: integer, default: 0 } }
        - { name: size, in: query, schema: { type: integer, default: 20 } }
      responses:
        '200':
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/RankingEntry' }
  /api/reputation/events:
    post:
      tags: [Internal]
      summary: Ingestion d'événements (interne — winners.declared, verification.completed)
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/ReputationEvent' }
      responses:
        '202': { description: Événement pris en compte }
components:
  securitySchemes:
    bearerAuth: { type: http, scheme: bearer, bearerFormat: JWT }
  schemas:
    ReputationProfile:
      type: object
      properties:
        developerId: { type: string, format: uuid }
        score: { type: integer, description: "Score global agrégé" }
        challengesWon: { type: integer }
        submissionsPassed: { type: integer }
        skills: { type: array, items: { type: string } }
        updatedAt: { type: string, format: date-time }
    Badge:
      type: object
      properties:
        id: { type: string }
        code: { type: string }
        label: { type: string }
        awardedAt: { type: string, format: date-time }
    RankingEntry:
      type: object
      properties:
        rank: { type: integer }
        developerId: { type: string, format: uuid }
        displayName: { type: string }
        score: { type: integer }
    ReputationEvent:
      type: object
      required: [type, developerId]
      properties:
        type: { type: string, enum: [CHALLENGE_WON, SUBMISSION_PASSED, SUBMISSION_REJECTED] }
        developerId: { type: string, format: uuid }
        challengeId: { type: string, format: uuid }
        weight: { type: integer }
        occurredAt: { type: string, format: date-time }
```

---

## 3. Catalogue d'événements (RabbitMQ · Spring Cloud Stream)

> [!note] Principe
> Le flux cœur (**soumission → vérification → jury → gagnants**) est **entièrement événementiel**. Les producteurs publient un fait métier (passé, immuable) ; les consommateurs réagissent sans couplage direct. Le nom des événements est au **passé**.

| Événement | Producteur | Consommateurs | Déclencheur |
|---|---|---|---|
| `challenge.published` | MS1 | MS2, MS4 | `POST /api/challenges/{id}/publish` |
| `application.registered` | MS2 | MS1, MS3 | `POST /api/challenges/{id}/applications` |
| `submission.submitted` | MS3 | **MS5**, MS4 | `POST /api/submissions/{id}/submit` |
| `verification.completed` | MS5 | MS3, MS4, MS2 | Fin du bac à sable |
| `submission.rejected` | MS5 | MS3, MS2 | Verdict `FAIL` |
| `similarity.flagged` | MS5 | MS4, MS3 | Similarité > seuil |
| `judging.completed` | MS4 | MS1, MS2 | Toutes les évaluations reçues |
| `winners.declared` | MS4 | MS1, MS2, **MS6** | `POST /api/challenges/{id}/declare-winners` |

### 3.1 Enveloppe commune

```json
{
  "eventId": "b1f2c3d4-...",
  "type": "submission.submitted",
  "occurredAt": "2026-11-11T10:15:30Z",
  "producer": "ms3",
  "version": 1,
  "payload": { }
}
```

### 3.2 Charges utiles

```jsonc
// challenge.published
{ "challengeId": "…", "enterpriseId": "…", "deadline": "2026-12-01T23:59:00Z", "tags": ["java","spring"] }

// application.registered
{ "challengeId": "…", "applicantType": "TEAM", "applicantId": "…" }

// submission.submitted
{ "submissionId": "…", "challengeId": "…", "applicantId": "…", "repoUrl": "https://github.com/acme/solution", "commitSha": "9f3c1a2" }

// verification.completed
{ "submissionId": "…", "challengeId": "…", "result": "PASS", "passed": 18, "failed": 0, "total": 18, "coverage": 0.87 }

// submission.rejected
{ "submissionId": "…", "challengeId": "…", "reason": "FAIL", "failed": 3 }

// similarity.flagged
{ "submissionId": "…", "challengeId": "…", "maxSimilarity": 0.94, "otherSubmissionId": "…" }

// judging.completed
{ "challengeId": "…", "submissionsCount": 7, "leaderboard": [{ "rank": 1, "submissionId": "…", "averageScore": 9.1 }] }

// winners.declared
{ "challengeId": "…", "winners": [
  { "rank": 1, "developerId": "…", "submissionId": "…", "prizeAmount": 5000 },
  { "rank": 2, "developerId": "…", "submissionId": "…", "prizeAmount": 2500 },
  { "rank": 3, "developerId": "…", "submissionId": "…", "prizeAmount": 1000 }
] }
```

### 3.3 Bindings Spring Cloud Stream (extrait `application.yml`)

```yaml
spring:
  cloud:
    function:
      definition: onSubmissionSubmitted;onVerificationCompleted;onWinnersDeclared
    stream:
      bindings:
        onSubmissionSubmitted-in-0:
          destination: submission.submitted
          group: ms5-verification
        onVerificationCompleted-in-0:
          destination: verification.completed
          group: ms4-evaluation
        onWinnersDeclared-in-0:
          destination: winners.declared
          group: ms6-reputation
        submissionSubmitted-out-0:
          destination: submission.submitted
      rabbit:
        bindings:
          onSubmissionSubmitted-in-0:
            consumer:
              autoBindDlq: true
              republishToDlq: true
```

> [!tip] Fiabilité
> Chaque consommateur utilise un **groupe** (file durable, un seul consommateur par groupe) ; les échecs partent en **DLQ** (`autoBindDlq`). Les producteurs sont **idempotents** via `eventId`.

---

## 4. `docker-compose.yml`

```yaml
# En Lice — orchestration locale complète (S5+)
# Lancement : docker compose up --build
name: enlice

networks:
  enlice-net:
    driver: bridge

volumes:
  mysql-data:
  pg-data:
  rabbit-data:

services:
  # ---------- Infrastructure ----------
  eureka:
    image: enlice/eureka:latest
    build: ./infra/eureka
    container_name: enlice-eureka
    ports: ["8761:8761"]
    networks: [enlice-net]

  keycloak:
    image: quay.io/keycloak/keycloak:25.0
    container_name: enlice-keycloak
    command: ["start-dev", "--import-realm"]
    environment:
      KC_BOOTSTRAP_ADMIN_USERNAME: admin
      KC_BOOTSTRAP_ADMIN_PASSWORD: admin
      KC_HTTP_PORT: 8080
    volumes:
      - ./infra/keycloak/realm-enlice.json:/opt/keycloak/data/import/realm-enlice.json:ro
    ports: ["8180:8080"]
    networks: [enlice-net]

  rabbitmq:
    image: rabbitmq:3.13-management
    container_name: enlice-rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: enlice
      RABBITMQ_DEFAULT_PASS: enlice
    volumes:
      - rabbit-data:/var/lib/rabbitmq
    ports:
      - "5672:5672"     # AMQP
      - "15672:15672"   # Console d'administration
    networks: [enlice-net]

  mysql:
    image: mysql:8.4
    container_name: enlice-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: enlice_evaluation
      MYSQL_USER: enlice
      MYSQL_PASSWORD: enlice
    volumes: [mysql-data:/var/lib/mysql]
    ports: ["3306:3306"]
    networks: [enlice-net]

  postgres:
    image: pgvector/pgvector:pg16
    container_name: enlice-postgres
    environment:
      POSTGRES_DB: enlice_verification
      POSTGRES_USER: enlice
      POSTGRES_PASSWORD: enlice
    volumes: [pg-data:/var/lib/postgresql/data]
    ports: ["5432:5432"]
    networks: [enlice-net]

  # ---------- Passerelle ----------
  gateway:
    image: enlice/gateway:latest
    build: ./services/gateway
    container_name: enlice-gateway
    depends_on: [eureka, keycloak]
    environment:
      EUREKA_URI: http://eureka:8761/eureka
      KEYCLOAK_ISSUER_URI: http://keycloak:8080/realms/enlice
    ports: ["8080:8080"]
    networks: [enlice-net]

  # ---------- Microservices ----------
  ms1-challenges:
    image: enlice/ms1-challenges:latest
    build: ./services/ms1-challenges
    container_name: enlice-ms1
    depends_on: [eureka, rabbitmq]
    environment:
      EUREKA_URI: http://eureka:8761/eureka
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_PROFILES_ACTIVE: docker
    ports: ["8081:8081"]
    networks: [enlice-net]

  ms2-participants:
    image: enlice/ms2-participants:latest
    build: ./services/ms2-participants
    container_name: enlice-ms2
    depends_on: [eureka, rabbitmq]
    environment:
      EUREKA_URI: http://eureka:8761/eureka
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_PROFILES_ACTIVE: docker
    ports: ["8082:8082"]
    networks: [enlice-net]

  ms3-submissions:
    image: enlice/ms3-submissions:latest
    build: ./services/ms3-submissions
    container_name: enlice-ms3
    depends_on: [eureka, rabbitmq]
    environment:
      EUREKA_URI: http://eureka:8761/eureka
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_PROFILES_ACTIVE: docker
    ports: ["8083:8083"]
    networks: [enlice-net]

  ms4-evaluation:
    image: enlice/ms4-evaluation:latest
    build: ./services/ms4-evaluation
    container_name: enlice-ms4
    depends_on: [eureka, rabbitmq, mysql]
    environment:
      EUREKA_URI: http://eureka:8761/eureka
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/enlice_evaluation
      SPRING_DATASOURCE_USERNAME: enlice
      SPRING_DATASOURCE_PASSWORD: enlice
      SPRING_PROFILES_ACTIVE: docker
    ports: ["8084:8084"]
    networks: [enlice-net]

  ms5-verification:
    image: enlice/ms5-verification:latest
    build: ./services/ms5-verification
    container_name: enlice-ms5
    depends_on: [eureka, rabbitmq, postgres]
    environment:
      EUREKA_URI: http://eureka:8761/eureka
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/enlice_verification
      SPRING_DATASOURCE_USERNAME: enlice
      SPRING_DATASOURCE_PASSWORD: enlice
      SANDBOX_ENABLED: "true"
      AI_ENABLED: "false"          # l'IA est optionnelle : la plateforme reste utilisable
      SPRING_PROFILES_ACTIVE: docker
    ports: ["8085:8085"]
    networks: [enlice-net]

  ms6-reputation:
    image: enlice/ms6-reputation:latest
    build: ./services/ms6-reputation
    container_name: enlice-ms6
    depends_on: [eureka, rabbitmq]
    environment:
      EUREKA_URI: http://eureka:8761/eureka
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_PROFILES_ACTIVE: docker
    ports: ["8086:8086"]
    networks: [enlice-net]

  # ---------- Frontend ----------
  frontend:
    image: enlice/frontend:latest
    build: ./frontend
    container_name: enlice-frontend
    depends_on: [gateway]
    ports: ["3000:80"]
    networks: [enlice-net]
```

**Variables d'environnement (`.env` à la racine) :**

```dotenv
# Secrets locaux uniquement — ne jamais committer de secrets réels
KC_ADMIN=admin
KC_ADMIN_PASSWORD=admin
RABBITMQ_USER=enlice
RABBITMQ_PASSWORD=enlice
MYSQL_ROOT_PASSWORD=root
POSTGRES_PASSWORD=enlice
```

**Commandes utiles :**

```bash
docker compose up --build -d          # démarrer toute la plateforme
docker compose stop ms5-verification  # vérifier la dégradation gracieuse (IA/anti-plagiat)
docker compose logs -f ms5-verification
docker compose down -v                # arrêter et supprimer les volumes
```

> [!success] Test de résilience (démonstration S10)
> `docker compose stop ms5-verification` : la plateforme reste **utilisable** (vérification manuelle possible) ; seuls les badges « ✨ IA » et l'anti-plagiat disparaissent. C'est la preuve que **l'IA est une amélioration, pas le cœur du système**.

---

## 5. Prochaines étapes

- [ ] Squelettes Spring Boot par service (`start.spring.io`) avec dépendances : Web, Eureka Client, OpenFeign, Spring Cloud Stream (RabbitMQ), Spring Security (OAuth2 Resource Server), springdoc-openapi, base de données.
- [ ] `realm-enlice.json` (realm `enlice`, rôles `DEVELOPER`/`ENTERPRISE`/`EXPERT`/`ADMIN`, clients frontend & gateway).
- [ ] Remplacer les placeholders **M1…M5** par les noms réels.
- [ ] Mocks (WireMock) pour permettre le développement indépendant avant intégration.
- [ ] Maquettes du frontend (Angular 21 / Node 22).

> [!note] Documents liés
> - [[requirements|Dossier de conception S4]]
> - [[En-Lice-FR|Description du projet]]