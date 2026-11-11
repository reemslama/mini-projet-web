package com.enlice.challenges.events;

import java.util.List;
import java.util.UUID;

/** Payloads consommés par MS1 (tolérants aux champs supplémentaires). */
public final class IncomingPayloads {

    private IncomingPayloads() {
    }

    public record ApplicationRegistered(UUID challengeId, String applicantType, UUID applicantId) {
    }

    public record JudgingCompleted(UUID challengeId, Integer submissionsCount) {
    }

    public record WinnersDeclared(UUID challengeId, List<Winner> winners) {
    }

    public record Winner(Integer rank, UUID developerId, UUID submissionId, Double prizeAmount) {
    }
}
