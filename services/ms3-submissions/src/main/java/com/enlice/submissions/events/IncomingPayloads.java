package com.enlice.submissions.events;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Payloads consommés par MS3 (tolérants aux champs supplémentaires). */
public final class IncomingPayloads {

    private IncomingPayloads() {
    }

    public record SubmissionSubmitted(UUID submissionId, UUID challengeId, UUID applicantId, String repoUrl,
                                      String commitSha) {
    }

    public record VerificationCompleted(UUID submissionId, UUID challengeId, String result, Integer passed,
                                        Integer failed, Integer total, Double coverage) {
    }

    public record SubmissionRejected(UUID submissionId, UUID challengeId, String reason, Integer failed) {
    }

    public record SimilarityFlagged(UUID submissionId, UUID challengeId, Double maxSimilarity, UUID otherSubmissionId) {
    }

    public record ChallengePublished(UUID challengeId, String status, java.time.Instant deadline,
                                     Integer applicationCount) {
    }
}
