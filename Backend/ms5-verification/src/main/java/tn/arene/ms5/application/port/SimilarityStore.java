package tn.arene.ms5.application.port;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import tn.arene.ms5.domain.SimilarityReport;

/** Persistance des rapports et des vecteurs (pgvector en production). */
public interface SimilarityStore {

	SimilarityReport save(SimilarityReport report, double threshold);

	Optional<SimilarityReport> findLatest(UUID submissionId);

	void saveEmbedding(UUID submissionId, UUID challengeId, String chunkId, String sourcePath,
			String contentSha256, String modelVersion, double[] vector);

	Map<UUID, Double> scoresForChallenge(UUID challengeId, UUID targetSubmission, double[] targetVector,
			String modelVersion, int topK);
}
