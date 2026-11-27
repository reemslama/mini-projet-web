package tn.arene.reputation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tn.arene.reputation.entities.ReputationEventType;

import java.time.LocalDateTime;
import java.util.UUID;

/** Contrat ReputationEvent (contracts/ms6-reputation.openapi.yaml). */
public record ReputationEventRequest(
        @NotNull ReputationEventType type,
        @NotNull UUID developerId,
        UUID challengeId,
        @Min(0) Integer weight,
        LocalDateTime occurredAt) {
}
