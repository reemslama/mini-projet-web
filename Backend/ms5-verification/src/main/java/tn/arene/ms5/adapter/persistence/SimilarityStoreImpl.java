package tn.arene.ms5.adapter.persistence;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import tn.arene.ms5.application.port.SimilarityStore;
import tn.arene.ms5.domain.SimilarityPolicy;
import tn.arene.ms5.domain.SimilarityReport;

@Repository
@Transactional
public class SimilarityStoreImpl implements SimilarityStore {

	private final SimilarityReportJpaRepository reports;
	private final CodeEmbeddingJpaRepository embeddings;

	public SimilarityStoreImpl(SimilarityReportJpaRepository reports, CodeEmbeddingJpaRepository embeddings) {
		this.reports = reports;
		this.embeddings = embeddings;
	}

	@Override
	public SimilarityReport save(SimilarityReport report, double threshold) {
		return reports.save(SimilarityReportEntity.from(report, threshold)).toDomain();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<SimilarityReport> findLatest(UUID submissionId) {
		return reports.findFirstBySubmissionIdOrderByAnalyzedAtDesc(submissionId)
				.map(SimilarityReportEntity::toDomain);
	}

	@Override
	public void saveEmbedding(UUID submissionId, UUID challengeId, String chunkId, String sourcePath,
			String contentSha256, String modelVersion, double[] vector) {
		CodeEmbeddingEntity entity = new CodeEmbeddingEntity();
		entity.setId(UUID.randomUUID());
		entity.setSubmissionId(submissionId);
		entity.setChallengeId(challengeId);
		entity.setChunkId(chunkId);
		entity.setSourcePath(sourcePath);
		entity.setContentSha256(contentSha256);
		entity.setModelVersion(modelVersion);
		entity.setVectorText(serialize(vector));
		embeddings.save(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, Double> scoresForChallenge(UUID challengeId, UUID targetSubmission, double[] targetVector,
			String modelVersion, int topK) {
		Map<UUID, Double> best = new HashMap<>();
		for (CodeEmbeddingEntity candidate : embeddings.findByChallengeIdAndModelVersionAndSubmissionIdNot(
				challengeId, modelVersion, targetSubmission)) {
			double score = SimilarityPolicy.cosine(targetVector, parse(candidate.getVectorText()));
			best.merge(candidate.getSubmissionId(), score, Math::max);
		}
		return best.entrySet().stream()
				.sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
				.limit(topK)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, HashMap::new));
	}

	private static String serialize(double[] vector) {
		StringBuilder builder = new StringBuilder();
		for (double value : vector) {
			if (builder.length() > 0) builder.append(',');
			builder.append(Double.toString(value));
		}
		return builder.toString();
	}

	private static double[] parse(String text) {
		return Arrays.stream(text.split(",")).mapToDouble(Double::parseDouble).toArray();
	}
}
