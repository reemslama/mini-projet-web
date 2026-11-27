package tn.arene.reputation.services.impl;

import tn.arene.reputation.entities.Badge;
import tn.arene.reputation.entities.ReputationProfile;

/** Regles d'attribution automatique des badges, evaluees apres chaque evenement. */
final class BadgeRules {

    private BadgeRules() {
    }

    static void applyTo(ReputationProfile p) {
        if (p.getSubmissionsPassed() >= 1) p.addBadge(Badge.award("FIRST_PASS", "Premiere soumission validee"));
        if (p.getChallengesWon() >= 1) p.addBadge(Badge.award("FIRST_WIN", "Premier challenge gagne"));
        if (p.getScore() >= 200) p.addBadge(Badge.award("RISING_STAR", "200 points atteints"));
        if (p.getScore() >= 1000) p.addBadge(Badge.award("LEGEND", "1000 points atteints"));
    }
}
