# Acène— MS4 Evaluation

## 1. Informations générales

- **Étudiante :** Rim Slama
- **Classe :** 4SSA16
- **Projet :** FORGE
- **Microservice :** MS4 – Evaluation
- **Branche GitHub :** rim-slama

## 2. Présentation

Le microservice MS4 Evaluation assure la gestion des évaluations, des experts, des jurys, des critères d'évaluation, des scores et des gagnants au sein de la plateforme FORGE.

## 3. Technologies utilisées

- Java 17
- Spring Boot 3.5.0
- Spring Cloud 2025.0.0
- Spring Data JPA
- MySQL / MariaDB
- Netflix Eureka
- Spring Boot Actuator
- Swagger / OpenAPI
- Lombok
- Maven
- Git et GitHub

## 4. Entités du microservice

| Entité | Description |
|---|---|
| Expert | Gestion des experts |
| Jury | Gestion des jurys |
| Evaluation | Gestion des évaluations |
| Rubric | Grille d'évaluation |
| RubricCriterion | Critères d'évaluation |
| Score | Notes attribuées |
| Winner | Gestion des gagnants |

## 5. Architecture et ports

| Service | Port |
|---|---|
| Eureka Server – Instance 1 | 8761 |
| Eureka Server – Instance 2 | 8762 |
| MS4 Evaluation – Instance 1 | 8084 |
| MS4 Evaluation – Instance 2 | 8085 |
| MySQL | 3306 |

Base de données : `forge_evaluation`

## 6. Configuration Eureka

Le microservice MS4 est enregistré auprès de deux serveurs Eureka configurés en mode Peer-to-Peer.

Les deux instances du microservice sont visibles dans les dashboards Eureka.

### Eureka Server 1

http://localhost:8761

![Eureka Server 1](screenshots/eureka-8761.png)

### Eureka Server 2

http://localhost:8762

![Eureka Server 2](screenshots/eureka-8762.png)

## 7. Tests CRUD

Un CRUD complet a été implémenté pour l'entité Expert.

| Méthode HTTP | Fonctionnalité | Résultat |
|---|---|---|
| POST | Ajouter un expert | Validé |
| GET | Consulter les experts | Validé |
| GET | Rechercher un expert par UUID | Validé |
| PUT | Modifier un expert | Validé |
| DELETE | Supprimer un expert | Validé |

### Swagger UI

http://localhost:8084/swagger-ui.html

![Swagger CRUD](screenshots/swagger-crud.png)

### Base de données

![Base de données](screenshots/database.png)

## 8. Spring Boot Actuator

Spring Boot Actuator permet de superviser l'état du microservice.

| Endpoint | Description |
|---|---|
| /actuator | Liste des endpoints disponibles |
| /actuator/health | État de santé du microservice |
| /actuator/info | Informations sur l'application |
| /actuator/metrics | Métriques de fonctionnement |

### Health

http://localhost:8084/actuator/health

![Actuator Health](screenshots/actuator-health.png)

### Info

http://localhost:8084/actuator/info

![Actuator Info](screenshots/actuator-info.png)

### Metrics

http://localhost:8084/actuator/metrics

![Actuator Metrics](screenshots/actuator-metrics.png)

## 9. Simulation d'une panne

Une simulation de panne a été réalisée en arrêtant le serveur MySQL.

### État initial : UP

Les deux instances du microservice sont opérationnelles.

![Etat UP](screenshots/eureka-up.png)

### Arrêt de MySQL : DOWN

Actuator détecte l'indisponibilité de la base de données et l'état DOWN est propagé aux serveurs Eureka.

![Actuator DOWN](screenshots/actuator-down.png)

![Eureka DOWN](screenshots/eureka-down.png)

### Redémarrage de MySQL

Après le redémarrage de MySQL, les instances retrouvent automatiquement leur état UP.

![Recovery UP](screenshots/eureka-recovery.png)

## 10. Instructions de lancement

1. Démarrer MySQL.
2. Configurer les alias locaux eureka1 et eureka2 dans le fichier hosts.
3. Démarrer Eureka Server sur le port 8761.
4. Démarrer la deuxième instance Eureka sur le port 8762 avec sa configuration Peer-to-Peer.
5. Démarrer MS4 Evaluation sur le port 8084.
6. Démarrer la deuxième instance MS4 sur le port 8085.
7. Vérifier les dashboards Eureka.
8. Tester les opérations CRUD avec Swagger.
9. Vérifier les endpoints Actuator.

## 11. Résultats

- Implémentation des entités et associations JPA.
- CRUD Expert fonctionnel.
- Persistance des données.
- Enregistrement auprès des deux serveurs Eureka.
- Exécution de deux instances MS4.
- Supervision avec Spring Boot Actuator.
- Détection d'une panne MySQL.
- Retour automatique à l'état UP.

L'intégration avec les autres microservices du groupe et les tests sur les différentes machines restent à valider.

## 12. Auteur

**Rim Slama — MS4 Evaluation**

ESPRIT — 2026/2027
