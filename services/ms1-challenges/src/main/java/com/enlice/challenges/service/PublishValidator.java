package com.enlice.challenges.service;

import com.enlice.challenges.domain.ChallengeStatus;
import com.enlice.challenges.web.ConflictException;
import java.time.Instant;

/**
 * Règles pures de publication d'un défi (testables sans Spring ni base de données).
 */
public final class PublishValidator {

    private PublishValidator() {
    }

    /**
     * @throws ConflictException si le défi ne peut pas être publié (409)
     */
    public static void validate(ChallengeStatus status, long criteriaCount, long prizeCount,
                                Instant deadline, Instant now) {
        if (status != ChallengeStatus.DRAFT) {
            throw new ConflictException("Le défi ne peut être publié que depuis le statut DRAFT (statut actuel : "
                    + status + ")");
        }
        if (criteriaCount <= 0) {
            throw new ConflictException("Impossible de publier : aucun critère d'acceptation n'est défini");
        }
        if (prizeCount <= 0) {
            throw new ConflictException("Impossible de publier : aucune prime n'est définie");
        }
        if (deadline == null || !deadline.isAfter(now)) {
            throw new ConflictException("Impossible de publier : la date limite est dépassée");
        }
    }
}
