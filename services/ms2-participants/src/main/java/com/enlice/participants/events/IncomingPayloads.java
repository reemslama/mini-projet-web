package com.enlice.participants.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Payloads consommés par MS2 (tolérants aux champs supplémentaires). */
public final class IncomingPayloads {

    private IncomingPayloads() {
    }

    public record ApplicationRegistered(UUID challengeId, String applicantType, UUID applicantId) {
    }

    public record ChallengePublished(UUID challengeId, UUID enterpriseId, Instant deadline, List<String> tags,
                                     String status, Instant publishedAt, Integer applicationCount) {
    }

    public record VerificationCompleted(UUID submissionId, UUID challengeId, UUID applicantId, String result,
                                         Integer passed, Integer failed, Integer total, Double coverage) {
    }

    public record SubmissionRejected(UUID submissionId, UUID challengeId, UUID applicantId, String reason,
                                     Integer failed) {
    }
}
