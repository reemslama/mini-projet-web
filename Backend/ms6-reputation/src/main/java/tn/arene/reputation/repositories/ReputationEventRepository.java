package tn.arene.reputation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.arene.reputation.entities.ReputationEvent;
import tn.arene.reputation.entities.ReputationEventType;

import java.util.UUID;

public interface ReputationEventRepository extends JpaRepository<ReputationEvent, Long> {

    /** Cle d'idempotence (type, developerId, challengeId). */
    boolean existsByTypeAndDeveloperIdAndChallengeId(ReputationEventType type, UUID developerId, UUID challengeId);
}
