# Arène · Présentation de conception (Séance 4)

Présentation web (React + reveal.js) du dossier de conception **S4 / Jalon 1** du projet
**Arène · Plateforme de hackathons & bounties** (module *Applications Web Distribuées · MT-41*).

Construite avec **[`@revealjs/react`](https://revealjs.com/react/)** (wrapper React officiel de
reveal.js 6) et **Vite**, avec un thème personnalisé « ardoise & rouge » — papier froid,
typographie entièrement serif (**EB Garamond**, titres et corps), accents rouges, code en
JetBrains Mono.

## Démarrage

```bash
npm install
npm run dev      # http://localhost:5173
```

## Scripts

| Commande | Effet |
|---|---|
| `npm run dev` | Serveur de développement (rechargement à chaud) |
| `npm run build` | Build de production dans `dist/` |
| `npm run preview` | Sert le build de production localement |

## Commandes de présentation (reveal.js)

| Touche | Action |
|---|---|
| `→` / `Espace` | Diapositive suivante |
| `←` | Diapositive précédente |
| `F` | Plein écran |
| `S` | Mode présentateur (notes de l’orateur) |
| `O` / `Échap` | Vue d’ensemble |
| `?` | Aide raccourcis |

**Export PDF :** ouvrir `http://localhost:5173/?print-pdf`, puis *Imprimer → Enregistrer au format PDF*.

**Export PPTX :** reveal.js n’a pas d’export PowerPoint natif. La méthode retenue est
*image par diapositive* (rendu fidèle au pixel, **non éditable**) : le PDF est rastérisé
puis chaque image est placée en pleine page dans un `.pptx` de 20 diapositives
(`presentation/export/`).

```bash
# 1. PDF (20 pages) depuis le serveur de dev
python mkpdf.py                     # ouvre ?print-pdf et écrit arena-presentation.pdf
# 2. rastérisation
pdftoppm -png -r 192 arena-presentation.pdf pages/page
# 3. assemblage PPTX
python mkpptx.py
```

> **Éditabilité.** La conversion `PDF → PPTX` de LibreOffice
> (`soffice --infilter="impress_pdf_import"`) produit bien 20 diapositives avec du texte
> modifiable, mais la fidélité s’effondre : l’importeur vectorise chaque diapositive en
> plusieurs centaines de formes et peint le fond en noir. Pour un PPTX **réellement
> éditable**, il faut reconstruire les diapositives nativement (python-pptx) plutôt que
> d’importer le PDF.

## Structure

```
presentation/
├── index.html                          # page hôte
├── vite.config.ts
├── tsconfig.json
├── export/                             # livrables générés
│   ├── Arène-S4-presentation.pdf     # 20 pages (954 × 557 pt)
│   └── Arène-S4-presentation.pptx    # 20 diapositives, image pleine page
└── src/
    ├── main.tsx                        # bootstrap React (sans StrictMode)
    ├── App.tsx                         # les 20 diapositives
    ├── theme.css                       # thème « ardoise & rouge » d'Arène
    └── components/
        └── ArchitectureDiagram.tsx     # diagramme d'architecture (SVG inline)
```

## Diapositives

1. Titre · Arène
2. Sommaire
3. Idée & besoin
4. Utilisateurs
5. Scénario de bout en bout
6. Périmètre MoSCoW
7. Architecture globale (diagramme)
8. Microservices (5 + 1 techno avancée)
9. Stratégie de données (une base par service)
10. Communication synchrone (Feign)
11. Communication asynchrone (RabbitMQ)
12. Technologie avancée MS5 (vérification)
13. Gateway, sécurité & configuration
14. Contrats d’API — cœur (MS1–MS3)
15. Contrats d’API — jury & avancé (MS4–MS6)
16. Stack technologique
17. Répartition des tâches
18. Pilotage & organisation Git
19. Planning & risques
20. Merci / prochaines étapes

## À personnaliser avant la soutenance

- Remplacer les placeholders **M1…M5** par les noms réels (dans `src/App.tsx`, diapositive 17, et
  dans `../requirements.md`).
- Ajuster l’année / la promotion sur la diapositive de titre si besoin.

## Dépannage

Si `npm run build` échoue avec `Cannot find module '@rolldown/binding-linux-x64-gnu'`
(binding natif optionnel de Vite 8 manquant), l’installer explicitement :

```bash
npm install @rolldown/binding-linux-x64-gnu@1.2.19
```
