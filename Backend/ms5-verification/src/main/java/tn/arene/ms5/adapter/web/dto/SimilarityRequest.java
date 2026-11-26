package tn.arene.ms5.adapter.web.dto;

import java.util.UUID;

public record SimilarityRequest(UUID submissionId, UUID challengeId) {
}
