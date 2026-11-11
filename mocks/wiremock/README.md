# Mocks WireMock

Mocks HTTP pour développer un service sans dépendre des autres (cf. §5 du document de contrats).

## GitHub (MS3)

MS3 fige le commit d'une soumission via l'API GitHub. Le stub `github-commit.json` répond à
`GET /repos/{owner}/{repo}/commits/{branch}` avec un SHA déterministe.

```bash
docker compose --profile mocks up -d wiremock
# puis, pour MS3 :
GITHUB_API_BASE_URL=http://localhost:8090 mvn spring-boot:run
# ou, dans le réseau compose :
GITHUB_API_BASE_URL=http://wiremock:8080
```

Sans mock ni réseau, MS3 bascule sur un SHA déterministe calculé localement (dégradation gracieuse).

## Ajouter un stub

Déposer un fichier `*.json` dans `mappings/` au format WireMock, puis recharger :

```bash
curl -X POST http://localhost:8090/__admin/mappings/reset
```
