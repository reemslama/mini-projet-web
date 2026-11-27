package tn.arene.reputation.entities;

/** Types d'evenements recus de MS4 (winners.declared) et MS5 (verification.completed). */
public enum ReputationEventType {
    CHALLENGE_WON(100),
    SUBMISSION_PASSED(20),
    SUBMISSION_REJECTED(0);

    /** Poids par defaut si l'evenement n'en fournit pas. */
    private final int defaultWeight;

    ReputationEventType(int defaultWeight) {
        this.defaultWeight = defaultWeight;
    }

    public int getDefaultWeight() {
        return defaultWeight;
    }
}
