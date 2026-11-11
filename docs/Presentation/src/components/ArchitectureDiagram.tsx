/**
 * Arène - diagramme d'architecture global (SVG, thème « ardoise & rouge »).
 * Rendu inline pour rester net à l'écran comme à l'export PDF.
 */
export default function ArchitectureDiagram() {
  const accent = '#d90429'   // rouge
  const accent2 = '#ef233c'  // rouge clair
  const ink = '#2b2d42'      // bleu ardoise (texte fort)
  const muted = '#5f6878'
  const line = 'rgba(141,153,174,0.55)'
  const panel = '#ffffff'
  const panelStroke = '#d3dce1'

  return (
    <div className="arch">
      <svg viewBox="0 0 1100 600" role="img" aria-label="Architecture microservices d'Arène">
        <defs>
          <marker id="arrow" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto" markerUnits="strokeWidth">
            <path d="M0,0 L8,3 L0,6 Z" fill={accent2} />
          </marker>
          <marker id="arrowEmber" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto" markerUnits="strokeWidth">
            <path d="M0,0 L8,3 L0,6 Z" fill={accent} />
          </marker>
          <linearGradient id="emberGrad" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#2b2d42" />
            <stop offset="100%" stopColor="#3d4160" />
          </linearGradient>
        </defs>

        {/* ---------- Client ---------- */}
        <rect x="430" y="16" width="240" height="54" rx="8" fill={panel} stroke={panelStroke} />
        <text x="550" y="49" textAnchor="middle" fill={ink} fontSize="17" fontWeight="600">
          Frontend SPA
        </text>

        <line x1="550" y1="70" x2="550" y2="104" stroke={accent2} strokeWidth="2" markerEnd="url(#arrow)" />

        {/* ---------- Gateway ---------- */}
        <rect x="400" y="106" width="300" height="56" rx="8" fill="url(#emberGrad)" />
        <text x="550" y="132" textAnchor="middle" fill="#edf2f4" fontSize="17" fontWeight="700">
          API Gateway
        </text>
        <text x="550" y="151" textAnchor="middle" fill="#b9c1d1" fontSize="11" fontFamily="EB Garamond, Georgia, serif">
          Spring Cloud Gateway · routage + JWT
        </text>

        {/* ---------- Keycloak ---------- */}
        <rect x="740" y="106" width="180" height="56" rx="8" fill={panel} stroke={panelStroke} />
        <text x="830" y="132" textAnchor="middle" fill={accent} fontSize="15" fontWeight="600">Keycloak</text>
        <text x="830" y="150" textAnchor="middle" fill={muted} fontSize="10.5" fontFamily="EB Garamond, Georgia, serif">Identity · OIDC</text>
        <line x1="700" y1="134" x2="740" y2="134" stroke={line} strokeWidth="1.6" strokeDasharray="4 4" markerEnd="url(#arrow)" />

        {/* ---------- Eureka ---------- */}
        <rect x="180" y="106" width="180" height="56" rx="8" fill={panel} stroke={panelStroke} />
        <text x="270" y="132" textAnchor="middle" fill={accent} fontSize="15" fontWeight="600">Eureka</text>
        <text x="270" y="150" textAnchor="middle" fill={muted} fontSize="10.5" fontFamily="EB Garamond, Georgia, serif">Discovery</text>
        <line x1="400" y1="134" x2="360" y2="134" stroke={line} strokeWidth="1.6" strokeDasharray="4 4" markerEnd="url(#arrow)" />

        {/* gateway -> services fan */}
        <path d="M550 162 L550 190" stroke={accent2} strokeWidth="2" />
        <path d="M120 190 L980 190" stroke={accent2} strokeWidth="2" opacity="0.55" />

        {/* ---------- Services ---------- */}
        {[
          { x: 40, n: 'MS1', t: 'Challenges', d: 'H2', owner: 'M1' },
          { x: 200, n: 'MS2', t: 'Participants', d: 'H2', owner: 'M2' },
          { x: 360, n: 'MS3', t: 'Soumissions', d: 'H2', owner: 'M3' },
          { x: 520, n: 'MS4', t: 'Évaluation', d: 'MySQL', owner: 'M4' },
          { x: 680, n: 'MS5', t: 'Vérification', d: 'PostgreSQL', owner: 'Équipe', adv: true },
          { x: 840, n: 'MS6', t: 'Réputation', d: 'H2', owner: 'M5' },
        ].map((s) => (
          <g key={s.n}>
            <line x1={s.x + 55} y1="190" x2={s.x + 55} y2="214" stroke={accent2} strokeWidth="1.8" markerEnd="url(#arrow)" />
            <rect x={s.x} y="216" width="130" height="86" rx="8"
              fill={panel}
              stroke={s.adv ? accent : panelStroke}
              strokeWidth={s.adv ? 2 : 1}
            />
            <text x={s.x + 65} y="242" textAnchor="middle" fill={accent} fontSize="13" fontWeight="700" fontFamily="EB Garamond, Georgia, serif">{s.n}</text>
            <text x={s.x + 65} y="264" textAnchor="middle" fill={ink} fontSize="14" fontWeight="600">{s.t}</text>
            <text x={s.x + 65} y="285" textAnchor="middle" fill={muted} fontSize="10.5" fontFamily="EB Garamond, Georgia, serif">{s.d}</text>
            <text x={s.x + 65} y="297" textAnchor="middle" fill={s.adv ? accent : muted} fontSize="9.5" fontFamily="EB Garamond, Georgia, serif">
              {s.owner}{s.adv ? ' · avancé' : ''}
            </text>
          </g>
        ))}

        {/* ---------- Async bus ---------- */}
        <rect x="330" y="352" width="440" height="48" rx="8" fill="#fce7ea" stroke={accent} strokeWidth="1.6" />
        <text x="550" y="381" textAnchor="middle" fill={accent} fontSize="15" fontWeight="700" fontFamily="EB Garamond, Georgia, serif">
          RabbitMQ · événements
        </text>
        <text x="550" y="428" textAnchor="middle" fill={muted} fontSize="11.5" fontFamily="EB Garamond, Georgia, serif">
          8 événements · producteurs MS1 à MS5 · MS6 consommateur
        </text>
        <text x="550" y="448" textAnchor="middle" fill={muted} fontSize="11.5" fontFamily="EB Garamond, Georgia, serif">
          cœur : submission.submitted → verification.completed → winners.declared
        </text>

        {/* MS3 -> bus, MS5 <-> bus, MS4 -> bus */}
        <path d="M425 302 C425 330 470 336 470 352" fill="none" stroke={accent} strokeWidth="1.8" markerEnd="url(#arrowEmber)" />
        <path d="M745 302 C745 330 700 336 700 352" fill="none" stroke={accent} strokeWidth="1.8" markerEnd="url(#arrowEmber)" />
        <path d="M585 302 C585 328 610 338 610 352" fill="none" stroke={accent} strokeWidth="1.8" markerEnd="url(#arrowEmber)" />

        {/* MS1, MS2 -> bus (challenge.published, application.registered) */}
        <path d="M95 302 C95 340 200 372 330 376" fill="none" stroke={accent} strokeWidth="1.4" opacity="0.85" markerEnd="url(#arrowEmber)" />
        <path d="M255 302 C255 326 300 338 356 352" fill="none" stroke={accent} strokeWidth="1.4" opacity="0.85" markerEnd="url(#arrowEmber)" />

        {/* ---------- Sync coupling (Feign) ---------- */}
        <g transform="translate(600, 478)">
          <text x="0" y="0" fill={ink} fontSize="12.5" fontWeight="600" fontFamily="EB Garamond, Georgia, serif">
            Couplage synchrone (Feign)
          </text>
          <text x="0" y="22" fill={muted} fontSize="11" fontFamily="EB Garamond, Georgia, serif">
            MS5 → MS1 (spec de tests) · MS5 → MS3 (dépôt, verdict)
          </text>
          <text x="0" y="42" fill={muted} fontSize="11" fontFamily="EB Garamond, Georgia, serif">
            MS4 → MS2 et MS3 (données du jury)
          </text>
        </g>

        {/* legend */}
        <g transform="translate(40, 470)">
          <line x1="0" y1="0" x2="34" y2="0" stroke={accent2} strokeWidth="2" markerEnd="url(#arrow)" />
          <text x="44" y="4" fill={muted} fontSize="11" fontFamily="EB Garamond, Georgia, serif">REST (via Gateway)</text>
          <line x1="0" y1="26" x2="34" y2="26" stroke={accent} strokeWidth="2" markerEnd="url(#arrowEmber)" />
          <text x="44" y="30" fill={muted} fontSize="11" fontFamily="EB Garamond, Georgia, serif">Événements (asynchrone)</text>
          <line x1="0" y1="52" x2="34" y2="52" stroke={line} strokeWidth="1.6" strokeDasharray="5 5" markerEnd="url(#arrow)" />
          <text x="44" y="56" fill={muted} fontSize="11" fontFamily="EB Garamond, Georgia, serif">Liaison technique (JWT, Feign, Eureka)</text>
        </g>

        <text x="1060" y="560" textAnchor="end" fill={accent} fontSize="12" fontFamily="EB Garamond, Georgia, serif" opacity="0.9">
          1 base par service · aucun couplage par FK
        </text>
      </svg>
    </div>
  )
}
