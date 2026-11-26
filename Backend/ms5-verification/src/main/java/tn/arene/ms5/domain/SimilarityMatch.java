package tn.arene.ms5.domain;

import java.util.UUID;

/** Correspondance avec une autre soumission du même défi. */
public record SimilarityMatch(UUID otherSubmissionId, double similarity) {
}
