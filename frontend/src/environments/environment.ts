export const environment = {
  production: false,
  keycloak: {
    url: 'http://localhost:8180',
    realm: 'enlice',
    clientId: 'enlice-frontend'
  },
  api: {
    ms1: '/ms1',
    ms2: '/ms2',
    ms3: '/ms3',
    ms4: '/ms4',
    ms5: '/ms5',
    ms6: '/ms6'
  }
} as const;

/** Identifiant d'entreprise de démonstration (aucune entité Entreprise dans les contrats). */
export const DEMO_ENTERPRISE_ID = '11111111-1111-1111-1111-111111111111';
