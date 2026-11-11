import { Deck, Slide } from '@revealjs/react'
import RevealHighlight from 'reveal.js/plugin/highlight'
import RevealNotes from 'reveal.js/plugin/notes'
import RevealSearch from 'reveal.js/plugin/search'
import ArchitectureDiagram from './components/ArchitectureDiagram'

const deckConfig = {
  hash: true,
  slideNumber: 'c/t',
  progress: true,
  controls: true,
  center: true,
  transition: 'slide' as const,
  transitionSpeed: 'default' as const,
  backgroundTransition: 'fade' as const,
  width: 1200,
  height: 700,
  margin: 0.06,
  minScale: 0.2,
  maxScale: 1.6,
  // Export PDF/PPTX : une page par diapositive, fragments regroupés.
  pdfSeparateFragments: false,
  pdfMaxPagesPerSlide: 1,
  highlight: { theme: 'monokai' },
}

const services = [
  { id: 'MS1', name: 'Challenges', owner: 'M1', db: 'H2', desc: "Espace entreprise, défis (spec, critères, tags, dates, cycle de vie), budget et primes, catalogue." },
  { id: 'MS2', name: 'Participants', owner: 'M2', db: 'H2', desc: 'Développeurs, équipes, inscriptions et candidatures aux défis.' },
  { id: 'MS3', name: 'Soumissions', owner: 'M3', db: 'H2', desc: 'Dépôt GitHub, commit figé, artefacts, cycle de vie, contrôle de la date limite.' },
  { id: 'MS4', name: 'Évaluation', owner: 'M4', db: 'MySQL', desc: 'Experts, jury, grilles pondérées, scores, leaderboard, désignation des gagnants.' },
  { id: 'MS5', name: 'Vérification', owner: 'Équipe', db: 'PostgreSQL + pgvector', desc: 'Technologie avancée : tests en bac à sable, anti-plagiat, IA d’appoint.', adv: true },
  { id: 'MS6', name: 'Réputation', owner: 'M5', db: 'H2', desc: 'Profils, réalisations vérifiées, compétences, classements.' },
]

const events: [string, string, string][] = [
  ['challenge.published', 'MS1', 'MS2, MS4'],
  ['application.registered', 'MS2', 'MS1, MS3'],
  ['submission.submitted', 'MS3', 'MS5, MS4'],
  ['verification.completed', 'MS5', 'MS3, MS4, MS2, MS6'],
  ['submission.rejected', 'MS5', 'MS3, MS2'],
  ['judging.completed', 'MS4', 'MS1, MS2'],
  ['winners.declared', 'MS4', 'MS1, MS2, MS6'],
  ['similarity.flagged', 'MS5', 'MS4, MS3'],
]

// Inventaire complet des endpoints, méthode par méthode (39 au total).
const contracts: { id: string; name: string; eps: [string, string][] }[] = [
  {
    id: 'MS1', name: 'Challenges',
    eps: [
      ['POST', '/api/challenges'],
      ['GET', '/api/challenges'],
      ['GET', '/api/challenges/{id}'],
      ['PUT', '/api/challenges/{id}'],
      ['POST', '/api/challenges/{id}/publish'],
      ['GET·PUT', '/api/challenges/{id}/acceptance-criteria'],
      ['GET·PUT', '/api/challenges/{id}/prizes'],
    ],
  },
  {
    id: 'MS2', name: 'Participants',
    eps: [
      ['POST', '/api/developers'],
      ['GET', '/api/developers/{id}'],
      ['POST', '/api/teams'],
      ['GET', '/api/teams/{id}'],
      ['POST·GET', '/api/challenges/{challengeId}/applications'],
    ],
  },
  {
    id: 'MS3', name: 'Soumissions',
    eps: [
      ['POST', '/api/submissions'],
      ['GET', '/api/submissions'],
      ['GET', '/api/submissions/{id}'],
      ['POST', '/api/submissions/{id}/submit'],
      ['GET·PUT', '/api/submissions/{id}/verdict'],
    ],
  },
  {
    id: 'MS4', name: 'Évaluation',
    eps: [
      ['POST', '/api/experts'],
      ['POST·GET', '/api/challenges/{challengeId}/jury'],
      ['POST', '/api/rubrics'],
      ['GET', '/api/rubrics'],
      ['POST', '/api/evaluations'],
      ['GET', '/api/challenges/{challengeId}/leaderboard'],
      ['POST', '/api/challenges/{challengeId}/declare-winners'],
    ],
  },
  {
    id: 'MS5', name: 'Vérification',
    eps: [
      ['POST', '/api/verifications'],
      ['GET', '/api/verifications/{id}'],
      ['GET', '/api/verifications/{id}/logs'],
      ['POST', '/api/similarity/analyze'],
      ['GET', '/api/similarity/{submissionId}'],
      ['POST', '/api/ai/summarize/{submissionId}'],
    ],
  },
  {
    id: 'MS6', name: 'Réputation',
    eps: [
      ['GET', '/api/developers/{id}/reputation'],
      ['GET', '/api/developers/{id}/badges'],
      ['GET', '/api/rankings'],
      ['POST', '/api/reputation/events'],
    ],
  },
]

const members = [
  { who: 'M1', what: 'MS1 · Challenges', tasks: 'API défis, spec, critères, primes, catalogue, tests' },
  { who: 'M2', what: 'MS2 · Participants', tasks: 'API développeurs et équipes, inscriptions, validations' },
  { who: 'M3', what: 'MS3 · Soumissions', tasks: 'API soumissions, dépôt GitHub, cycle de vie, date limite' },
  { who: 'M4', what: 'MS4 · Évaluation', tasks: 'API jury, grilles, scores, leaderboard, gagnants' },
  { who: 'M5', what: 'MS6 · Réputation', tasks: 'API profils, badges, classements' },
  { who: 'Équipe', what: 'MS5 + Infra + Front', tasks: 'Bac à sable de tests, anti-plagiat, IA · Gateway, Keycloak, Eureka, Docker · écrans' },
]

const roadmap = [
  ['S4', 'Validation de conception (ce document)'],
  ['S5', 'Gateway, Eureka et Compose local'],
  ['S6', 'Communication synchrone (Feign)'],
  ['S7', 'Asynchrone (RabbitMQ) et pipeline de vérification'],
  ['S8', 'Sécurité Keycloak (realm, rôles, JWT)'],
  ['S9', 'Dockerisation complète et bac à sable'],
  ['S10', 'Démonstration et soutenance'],
]

const risks = [
  { level: 'Élevé', low: false, text: 'Exécution de code non fiable : conteneurs isolés (réseau coupé, mémoire et CPU limités, non-root, timeout). Variante : image Docker et commande fournies.' },
  { level: 'Moyen', low: true, text: 'Périmètre large : priorisation MoSCoW, le catalogue d’événements borne les responsabilités.' },
  { level: 'Moyen', low: true, text: 'Intégration tardive : contrats gelés en S4 et mocks WireMock.' },
  { level: 'Moyen', low: true, text: 'Démo dépendante d’API externes : jeux de données pré-remplis, profils mock.' },
]

function EndpointList({ svc }: { svc: (typeof contracts)[number] }) {
  return (
    <div className="cat-svc">
      <h3>{svc.id} · {svc.name}</h3>
      {svc.eps.map(([m, p]) => (
        <div className="ep" key={`${m}-${p}`}>
          <span className="m">{m}</span>
          <span className="p">{p}</span>
        </div>
      ))}
    </div>
  )
}

export default function App() {
  return (
    <Deck config={deckConfig} plugins={[RevealHighlight, RevealNotes, RevealSearch]}>
      {/* 1 - Titre */}
      <Slide>
        <div className="plate">
          <div className="plate-top">
            <span className="plate-mark">EL</span>
            <span className="plate-context">Applications Web Distribuées · MT-41</span>
          </div>
          <h1>Arène</h1>
          <p className="subtitle">Plateforme de hackathons &amp; bounties</p>
          <div className="plate-rule" />
          <p className="meta">
            Revue de cadrage et d’architecture · Séance 4, Jalon 1<br />
            Validation finale · 2026-2027
          </p>
        </div>
        <aside className="notes">
          Arène : une entreprise publie un défi avec un cahier des charges, une barre d’acceptation
          (tests) et une prime financée. Les développeurs soumettent un dépôt GitHub ; un moteur de
          vérification filtre automatiquement ; les experts désignent les gagnants.
        </aside>
      </Slide>

      {/* 2 - Sommaire */}
      <Slide>
        <h2>Ce que nous validons aujourd’hui</h2>
        <div className="index">
          <div className="index-item">
            <span className="index-n">01</span>
            <span className="index-t">Cadrage</span>
            <span className="index-d">Idée, besoin, utilisateurs, scénarios, périmètre MoSCoW.</span>
          </div>
          <div className="index-item">
            <span className="index-n">02</span>
            <span className="index-t">Architecture</span>
            <span className="index-d">Diagramme global, six microservices, stratégie de données.</span>
          </div>
          <div className="index-item">
            <span className="index-n">03</span>
            <span className="index-t">Communication</span>
            <span className="index-d">Synchrone (Feign) et asynchrone (RabbitMQ), Gateway et sécurité.</span>
          </div>
          <div className="index-item">
            <span className="index-n">04</span>
            <span className="index-t">Exécution</span>
            <span className="index-d">Contrats d’API, stack, répartition des tâches, planning, risques.</span>
          </div>
        </div>
      </Slide>

      {/* 3 - Idée & besoin */}
      <Slide>
        <h2>L’idée et le besoin</h2>
        <div className="split">
          <div className="split-col">
            <h3>Le problème</h3>
            <ul>
              <li>Une entreprise a un <strong>besoin logiciel ponctuel</strong>.</li>
              <li>Elle ne veut ni <strong>recruter une équipe</strong>, ni payer des freelances au forfait.</li>
              <li>Les développeurs veulent des <strong>projets réels</strong>, un <strong>prix</strong> et de la <strong>visibilité</strong>.</li>
            </ul>
          </div>
          <div className="split-col lead-col">
            <h3>La solution : Arène</h3>
            <ul>
              <li>L’entreprise publie un <strong>défi</strong> : spec, <strong>tests d’acceptation</strong> et <strong>prime (top 3)</strong>.</li>
              <li>Les développeurs soumettent leur <strong>dépôt GitHub</strong>.</li>
              <li>Un <strong>moteur de vérification</strong> rejette automatiquement les échecs.</li>
              <li>Les <strong>experts</strong> évaluent et désignent les <strong>gagnants</strong>.</li>
            </ul>
          </div>
        </div>
        <div className="note" style={{ marginTop: '0.9em' }}>
          Valeur métier : réduire le coût d’accès à du logiciel sur mesure, et offrir aux développeurs
          un portfolio <em>vérifiable</em>.
        </div>
      </Slide>

      {/* 4 - Utilisateurs */}
      <Slide>
        <h2>Les utilisateurs</h2>
        <div className="roster">
          <div className="roster-item">
            <div className="roster-k">Entreprise</div>
            <div className="roster-v">Obtenir un logiciel conforme, à coût maîtrisé, avec une qualité garantie.</div>
          </div>
          <div className="roster-item">
            <div className="roster-k">Développeur / Équipe</div>
            <div className="roster-v">Projets réels, prime, visibilité et profil valorisant pour l’embauche.</div>
          </div>
          <div className="roster-item">
            <div className="roster-k">Expert</div>
            <div className="roster-v">Évaluer les projets sur des critères clairs et pondérés.</div>
          </div>
          <div className="roster-item">
            <div className="roster-k">Admin</div>
            <div className="roster-v">Modérer, gérer les litiges et les comptes.</div>
          </div>
        </div>
        <p className="small" style={{ marginTop: '1.1em' }}>
          Rôles Keycloak : <span className="tag ink">DEVELOPER</span>
          <span className="tag ink">ENTERPRISE</span>
          <span className="tag ink">EXPERT</span>
          <span className="tag ink">ADMIN</span>
        </p>
        <p className="small" style={{ marginTop: '0.5em' }}>
          Chaque route est protégée par rôle (<code>@PreAuthorize</code>) : l’entreprise publie et
          finance, le développeur soumet, l’expert évalue, l’administrateur modère.
        </p>
      </Slide>

      {/* 5 - Scénario de bout en bout */}
      <Slide>
        <h2>Scénario de bout en bout</h2>
        <div className="rail">
          <div className="rail-step hot">
            <span className="rail-n">01</span>
            <div className="rail-t">L’entreprise publie un défi : spec, tests, primes, date limite.</div>
          </div>
          <div className="rail-step hot">
            <span className="rail-n">02</span>
            <div className="rail-t">Le développeur s’inscrit, seul ou en équipe.</div>
          </div>
          <div className="rail-step hot">
            <span className="rail-n">03</span>
            <div className="rail-t">Il soumet son dépôt GitHub, avec un commit figé.</div>
          </div>
          <div className="rail-step hot">
            <span className="rail-n">04</span>
            <div className="rail-t">Le moteur exécute les tests et rend un verdict.</div>
          </div>
          <div className="rail-step hot">
            <span className="rail-n">05</span>
            <div className="rail-t">Les experts notent les projets retenus.</div>
          </div>
          <div className="rail-step hot">
            <span className="rail-n">06</span>
            <div className="rail-t">Les gagnants (top 3) sont désignés et récompensés.</div>
          </div>
        </div>
        <div className="note" style={{ marginTop: '1.1em' }}>
          Un <span className="tag bad">FAIL</span> aux tests d’acceptation déclenche un <strong>rejet
          automatique</strong>. Les logs restent consultables par l’équipe.
        </div>
      </Slide>

      {/* 6 - Périmètre MoSCoW */}
      <Slide>
        <h2>Priorisation MoSCoW</h2>
        <div className="bands">
          <div className="band must">
            <div className="band-k">Must</div>
            <div className="band-v">Publier un défi · s’inscrire · soumettre un dépôt · <strong>exécuter les tests</strong> · verdict PASS/FAIL et rejet automatique · notation · classement · gagnants · auth Keycloak · Gateway et Eureka.</div>
          </div>
          <div className="band">
            <div className="band-k">Should</div>
            <div className="band-v">Réputation et badges · feedback du jury · tableau de bord entreprise · notifications d’événements · anti-plagiat.</div>
          </div>
          <div className="band">
            <div className="band-k">Could</div>
            <div className="band-v">Appariement experts et défis · résumé IA des soumissions · rédaction assistée des tests · messagerie · statistiques.</div>
          </div>
          <div className="band wont">
            <div className="band-k">Won’t</div>
            <div className="band-v">Paiement réel (simulé) · dépôts GitHub privés · exécution multi-langages · application mobile.</div>
          </div>
        </div>
      </Slide>

      {/* 7 - Architecture globale */}
      <Slide>
        <h2>Architecture globale</h2>
        <ArchitectureDiagram />
      </Slide>

      {/* 8 - Microservices */}
      <Slide>
        <h2>Décomposition en microservices</h2>
        <p className="lede">
          Cinq services portés individuellement, plus un sixième porté par l’équipe sur une
          technologie avancée.
        </p>
        <div className="tiles">
          {services.map((s) => (
            <div className={`tile${s.adv ? ' adv' : ''}`} key={s.id}>
              <div className="tile-head">
                <span className="tile-id">{s.id}</span>
                <span className="tile-owner">{s.owner}</span>
              </div>
              <div className="tile-name">
                {s.name}
                {s.adv && <span className="tag" style={{ marginLeft: '0.45em' }}>avancé</span>}
              </div>
              <div className="tile-desc">{s.desc}</div>
              <span className="tile-db">{s.db}</span>
            </div>
          ))}
        </div>
        <p className="small" style={{ marginTop: '0.9em' }}>
          Infrastructure partagée (équipe) : <strong>API Gateway</strong>, <strong>Keycloak</strong>,{' '}
          <strong>Eureka</strong>, <strong>Docker/Compose</strong>, <strong>Frontend</strong>.
        </p>
      </Slide>

      {/* 9 - Stratégie de données */}
      <Slide>
        <h2>Une base par service</h2>
        <table>
          <thead>
            <tr><th>Service</th><th>Base</th><th>Justification</th></tr>
          </thead>
          <tbody>
            <tr><td>MS1 · Challenges</td><td><code>H2</code></td><td>Données relationnelles simples (défis, primes)</td></tr>
            <tr><td>MS2 · Participants</td><td><code>H2</code></td><td>Entités légères (développeurs, équipes, candidatures)</td></tr>
            <tr><td>MS3 · Soumissions</td><td><code>H2</code></td><td>Métadonnées de dépôts, cycle de vie</td></tr>
            <tr><td>MS4 · Évaluation</td><td><code>MySQL</code></td><td>Scores, grilles et agrégations, donc un SGBD robuste</td></tr>
            <tr><td>MS5 · Vérification</td><td><code>PostgreSQL + pgvector</code></td><td>Résultats de tests (JSONB) et similarité vectorielle</td></tr>
            <tr><td>MS6 · Réputation</td><td><code>H2</code></td><td>Profils, badges, classements</td></tr>
          </tbody>
        </table>
        <div className="note alert" style={{ marginTop: '0.8em' }}>
          <strong>Règle d’or :</strong> une base par service, <em>aucune clé étrangère inter-services</em>.
          Les références croisées passent par identifiant et par événements.
        </div>
        <p className="small" style={{ marginTop: '0.7em' }}>
          Pas de transaction distribuée : la cohérence entre services est <strong>à terme</strong>,
          portée par les événements, et chaque service migre son propre schéma.
        </p>
      </Slide>

      {/* 10 - Communication synchrone */}
      <Slide>
        <h2>Communication synchrone</h2>
        <div className="calls">
          <div className="call">
            <div className="call-a">Frontend</div>
            <div className="call-arrow">→</div>
            <div className="call-b">Gateway</div>
            <div className="call-note">Routage et validation du JWT vers tous les services.</div>
          </div>
          <div className="call">
            <div className="call-a">MS5 · Vérification</div>
            <div className="call-arrow">→</div>
            <div className="call-b">MS1 · Challenges</div>
            <div className="call-note">Récupérer la spec de tests d’acceptation du défi.</div>
          </div>
          <div className="call">
            <div className="call-a">MS5 · Vérification</div>
            <div className="call-arrow">⇄</div>
            <div className="call-b">MS3 · Soumissions</div>
            <div className="call-note">Récupérer le dépôt de la soumission, puis pousser le verdict.</div>
          </div>
          <div className="call">
            <div className="call-a">MS4 · Évaluation</div>
            <div className="call-arrow">→</div>
            <div className="call-b">MS3 et MS2</div>
            <div className="call-note">Données de soumission et de participants pour le jury.</div>
          </div>
        </div>
        <div className="note alert" style={{ marginTop: '0.9em' }}>
          <strong>REST via OpenFeign.</strong> Appels directs via <strong>Eureka</strong>, hors Gateway.
          Relais du jeton pour les appels utilisateur, compte de service pour les traitements
          d’événement. Résilience : délais courts, réessais bornés, disjoncteur (<code>Resilience4j</code>).
        </div>
      </Slide>

      {/* 11 - Communication asynchrone */}
      <Slide>
        <h2>Communication asynchrone</h2>
        <p className="lede">RabbitMQ via Spring Cloud Stream : le flux cœur est entièrement événementiel.</p>
        <div className="stream">
          <div className="stream-head">
            <span>Événement</span><span>Producteur</span><span>Consommateurs</span>
          </div>
          {events.map(([e, p, c]) => (
            <div className="stream-row" key={e}>
              <span className="stream-evt">{e}</span>
              <span className="stream-prod">{p}</span>
              <span className="stream-cons">{c}</span>
            </div>
          ))}
        </div>
        <div className="note" style={{ marginTop: '0.6em' }}>
          <strong>Enveloppe commune :</strong> <code>eventId</code>, <code>type</code>,{' '}
          <code>occurredAt</code>, <code>producer</code>, <code>version</code>, <code>payload</code>.
          Un groupe durable par consommateur, une file de rebut en cas d’échec, des consommateurs{' '}
          <strong>idempotents</strong> par <code>eventId</code> : livraison « au moins une fois ».
        </div>
      </Slide>

      {/* 12 - Technologie avancée */}
      <Slide>
        <h2>MS5 : la technologie avancée</h2>
        <div className="feature">
          <div className="feature-main">
            <h3>Moteur de tests</h3>
            <p>
              Déclenché par <code>submission.submitted</code>, il récupère le dépôt et exécute les
              tests dans un <strong>conteneur isolé</strong>.
            </p>
            <p className="small">Verdict : <code>{'{PASS|FAIL, tests, couverture, logs}'}</code></p>
            <p className="small">
              Isolation : <code>--network none</code>, mémoire et CPU plafonnés, utilisateur non-root,
              système de fichiers en lecture seule, délai maximal strict.
            </p>
          </div>
          <div className="feature-side">
            <div className="feat">
              <h3>Anti-plagiat</h3>
              <p>
                Embeddings des soumissions et similarité cosinus via <strong>pgvector</strong> :
                alerte le jury avant la notation.
              </p>
            </div>
            <div className="feat">
              <h3>IA d’appoint</h3>
              <p>
                Résumé de soumission pour le jury, rédaction de tests à partir des exigences,
                appariement experts et défis.
              </p>
            </div>
          </div>
        </div>
        <div className="note good" style={{ marginTop: '1em' }}>
          <strong>L’IA est une amélioration, pas le cœur.</strong>{' '}
          <code>docker compose stop ms5-verification</code> : la plateforme reste utilisable en vérification
          manuelle ; seuls les badges IA et l’anti-plagiat disparaissent.
        </div>
      </Slide>

      {/* 13 - Gateway & sécurité */}
      <Slide>
        <h2>Gateway, sécurité et configuration</h2>
        <div className="split">
          <div className="split-col">
            <h3>API Gateway</h3>
            <div className="spec-row">
              <span className="spec-k">Rôle</span>
              <span className="spec-v">Point d’entrée unique, routage vers les services via Eureka.</span>
            </div>
            <div className="spec-row">
              <span className="spec-k">Sécurité</span>
              <span className="spec-v">Validation des JWT Keycloak, CORS, rate limiting léger.</span>
            </div>
          </div>
          <div className="split-col lead-col">
            <h3>Keycloak</h3>
            <div className="spec-row">
              <span className="spec-k">Realm</span>
              <span className="spec-v"><code>arena</code> et rôles DEVELOPER, ENTERPRISE, EXPERT, ADMIN.</span>
            </div>
            <div className="spec-row">
              <span className="spec-k">Clients</span>
              <span className="spec-v">Frontend public (Authorization Code + PKCE), gateway confidentiel.</span>
            </div>
          </div>
        </div>
        <div className="note alert" style={{ marginTop: '1em' }}>
          <strong>L’authentification relève de Keycloak (infrastructure), pas d’un microservice.</strong>{' '}
          On le configure, on ne le code pas : chaque service valide simplement le JWT
          (<code>SecurityFilterChain</code>, <code>@PreAuthorize</code>). Un service d’auth maison ne
          serait qu’un habillage, donc un anti-pattern.
        </div>
        <div className="note" style={{ marginTop: '0.7em' }}>
          <strong>Configuration et conventions.</strong> Un <code>Dockerfile</code> par service et un{' '}
          <code>docker-compose</code> de bout en bout, profils <code>dev</code> et <code>docker</code>,
          secrets par variables d’environnement. Erreurs <code>ErrorResponse</code>, pagination{' '}
          <code>page/size/sort</code>, versionnage <code>/api/v2</code>, idempotence par <code>eventId</code>.
        </div>
      </Slide>

      {/* 14 - Contrats d'API (cœur) */}
      <Slide>
        <h2>Contrats d’API : cœur</h2>
        <div className="cat">
          <div><EndpointList svc={contracts[0]} /></div>
          <div>
            <EndpointList svc={contracts[1]} />
            <EndpointList svc={contracts[2]} />
          </div>
        </div>
        <p className="small" style={{ marginTop: '0.8em' }}>
          Documentation <strong>Swagger / OpenAPI</strong> générée par <code>springdoc-openapi</code> sur
          chaque service. Les paramètres de chemin sont notés entre accolades.
        </p>
      </Slide>

      {/* 15 - Contrats d'API (jury & avancé) */}
      <Slide>
        <h2>Contrats d’API : jury et avancé</h2>
        <div className="cat">
          <div><EndpointList svc={contracts[3]} /></div>
          <div>
            <EndpointList svc={contracts[4]} />
            <EndpointList svc={contracts[5]} />
          </div>
        </div>
        <p className="small" style={{ marginTop: '0.8em' }}>
          Les méthodes partageant une même ressource sont groupées sur une seule ligne
          (<code>GET·PUT</code>, <code>POST·GET</code>).
        </p>
      </Slide>

      {/* 16 - Stack */}
      <Slide>
        <h2>Stack technologique</h2>
        <div className="specs">
          <div>
            <div className="spec-row">
              <span className="spec-k">Langage</span>
              <span className="spec-v"><b>Java et Spring Boot</b>, standard des microservices, imposé par le cours.</span>
            </div>
            <div className="spec-row">
              <span className="spec-k">Gateway</span>
              <span className="spec-v"><b>Spring Cloud Gateway</b> : routage, filtres, intégration Spring Security.</span>
            </div>
            <div className="spec-row">
              <span className="spec-k">Découverte</span>
              <span className="spec-v"><b>Eureka</b> : enregistrement, découverte, équilibrage côté client.</span>
            </div>
            <div className="spec-row">
              <span className="spec-k">Sécurité</span>
              <span className="spec-v"><b>Keycloak</b> : OIDC et JWT, rôles, standard éprouvé.</span>
            </div>
            <div className="spec-row">
              <span className="spec-k">Observabilité</span>
              <span className="spec-v"><b>Spring Boot Actuator</b> (<code>/health</code>), consoles Eureka et RabbitMQ, journaux corrélés par <code>eventId</code>.</span>
            </div>
          </div>
          <div>
            <div className="spec-row">
              <span className="spec-k">Communication</span>
              <span className="spec-v"><b>OpenFeign et RabbitMQ</b> : couvre les deux modes exigés.</span>
            </div>
            <div className="spec-row">
              <span className="spec-k">Bases</span>
              <span className="spec-v"><b>H2, MySQL, PostgreSQL et pgvector</b> : persistance par service, pgvector pour la similarité.</span>
            </div>
            <div className="spec-row">
              <span className="spec-k">Conteneurs</span>
              <span className="spec-v"><b>Docker et Compose</b> : lancement reproductible de bout en bout.</span>
            </div>
            <div className="spec-row">
              <span className="spec-k">Frontend</span>
              <span className="spec-v"><b>Angular 21 (Node 22)</b> : SPA consommant les API via la Gateway.</span>
            </div>
          </div>
        </div>
      </Slide>

      {/* 17 - Répartition des tâches */}
      <Slide>
        <h2>Répartition des tâches</h2>
        <div className="lineup">
          {members.map((m) => (
            <div className="lineup-row" key={m.who}>
              <span className="lineup-who">{m.who}</span>
              <span className="lineup-what">{m.what}</span>
              <span className="lineup-tasks">{m.tasks}</span>
            </div>
          ))}
        </div>
        <p className="small" style={{ marginTop: '0.8em' }}>
          Intégration : contrats gelés en S4, développement indépendant sur mocks, intégration S5-S9,
          démonstration S10.
        </p>
      </Slide>

      {/* 18 - Pilotage & Git */}
      <Slide>
        <h2>Gestion de projet et organisation Git</h2>
        <div className="split">
          <div className="split-col">
            <h3>GitHub Projects</h3>
            <div className="board">
              {['Backlog', 'À faire', 'En cours', 'Revue', 'Fait'].map((c, i) => (
                <div className={`board-col${i === 2 ? ' now' : ''}`} key={c}>
                  <span>{c}</span>
                </div>
              ))}
            </div>
            <ul className="small">
              <li>Un <strong>label par microservice</strong>.</li>
              <li>Une <strong>milestone par séance</strong>, de S4 à S10.</li>
              <li>Epics <em>Jalon 1</em> et <em>Jalon 2</em>.</li>
            </ul>
          </div>
          <div className="split-col lead-col">
            <h3>Git</h3>
            <div className="branch">main ← develop ← feature/msX-…</div>
            <ul className="small">
              <li>PR avec revue avant merge.</li>
              <li>Commits conventionnels : <code>feat(ms1): …</code>.</li>
              <li>Commits réguliers et <strong>traçables par membre</strong>, pour l’évaluation individuelle.</li>
              <li>Intégration continue (optionnelle) : build et tests à chaque PR.</li>
            </ul>
          </div>
        </div>
      </Slide>

      {/* 19 - Planning & risques */}
      <Slide>
        <h2>Feuille de route et points de vigilance</h2>
        <div className="plan">
          <div>
            <h4>Feuille de route</h4>
            {roadmap.map(([k, v]) => (
              <div className="tl" key={k}>
                <span className="tl-k">{k}</span>
                <span className="tl-v">{v}</span>
              </div>
            ))}
          </div>
          <div>
            <h4>Risques et mitigation</h4>
            {risks.map((r) => (
              <div className="risk" key={r.text}>
                <span className={`risk-k${r.low ? ' low' : ''}`}>{r.level}</span>
                <span className="risk-v">{r.text}</span>
              </div>
            ))}
          </div>
        </div>
      </Slide>

      {/* 20 - Merci */}
      <Slide>
        <div className="plate">
          <div className="plate-top">
            <span className="plate-mark">EL</span>
            <span className="plate-context">Merci de votre attention</span>
          </div>
          <h1>Merci</h1>
          <p className="subtitle">Entrez en arène.</p>
          <div className="plate-rule" />
          <p className="meta">
            Prochaines étapes · squelettes Spring Boot, docker-compose et maquettes frontend<br />
            Livrables S4 · ce dossier, le diagramme d’architecture et les contrats d’API<br />
            Place à la discussion.
          </p>
        </div>
      </Slide>
    </Deck>
  )
}
