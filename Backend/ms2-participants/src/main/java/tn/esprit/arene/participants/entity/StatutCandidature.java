package tn.esprit.arene.participants.entity;

/**
 * Cycle de vie d'une candidature :
 * EN_ATTENTE -> VALIDEE -> (VERIFIEE | REJETEE) -> GAGNANTE
 */
public enum StatutCandidature {
    EN_ATTENTE, VALIDEE, REJETEE, VERIFIEE, GAGNANTE
}
