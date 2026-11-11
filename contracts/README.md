# Contrats OpenAPI — gelés en S4

Ces fichiers sont extraits du document [`En-Lice-contrats-et-deploiement.md`](../En-Lice-contrats-et-deploiement.md)
et font foi pour l'implémentation. **Ne pas modifier sans nouvelle version de contrat.**

| Service | Fichier | Port | Swagger (direct) |
|---|---|---|---|
| MS1 Challenges | [`ms1-challenges.yaml`](openapi/ms1-challenges.yaml) | 8081 | http://localhost:8081/swagger-ui.html |
| MS2 Participants | [`ms2-participants.yaml`](openapi/ms2-participants.yaml) | 8082 | http://localhost:8082/swagger-ui.html |
| MS3 Soumissions | [`ms3-submissions.yaml`](openapi/ms3-submissions.yaml) | 8083 | http://localhost:8083/swagger-ui.html |
| MS4 Évaluation | [`ms4-evaluation.yaml`](openapi/ms4-evaluation.yaml) | 8084 | http://localhost:8084/swagger-ui.html |
| MS5 Vérification | [`ms5-verification.yaml`](openapi/ms5-verification.yaml) | 8085 | http://localhost:8085/swagger-ui.html |
| MS6 Réputation | [`ms6-reputation.yaml`](openapi/ms6-reputation.yaml) | 8086 | http://localhost:8086/swagger-ui.html |

Via la passerelle : `http://localhost:8080/msN/api/**`.

## Règles

- Base path `/api`, JSON, pagination `page/size/sort`.
- Auth `Authorization: Bearer <JWT>` (Keycloak, realm `enlice`).
- Erreur unique `ErrorResponse {timestamp, status, error, message, path}`.
- Les opérations sans `security:` dans le contrat sont publiques ; toutes les autres exigent un JWT.

## Vérifier la cohérence

```python
import glob, yaml
for f in glob.glob('contracts/openapi/*.yaml'):
    d = yaml.safe_load(open(f))
    assert d['openapi'] == '3.0.3' and d['paths'], f
    print(f, '→', len(d['paths']), 'paths')
PY
```
