package tn.arene.ms5.application.usecase;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tn.arene.ms5.application.NotFoundException;
import tn.arene.ms5.application.port.EmbeddingPort;
import tn.arene.ms5.application.port.EventPublisher;
import tn.arene.ms5.application.port.SimilarityStore;
import tn.arene.ms5.application.port.SubmissionGateway;
import tn.arene.ms5.domain.SimilarityMatch;
import tn.arene.ms5.domain.SimilarityPolicy;
import tn.arene.ms5.domain.SimilarityReport;

/** Use case 4/5 : similarité déterministe ; flagged n'affecte jamais le verdict. */
@Service
public class AnalyzeSimilarity {

	public record Request(UUID submissionId, UUID challengeId) {
	}

	private final SubmissionGateway submissions;
	private final SimilarityStore store;
	private final EmbeddingPort embedder;
	private final EventPublisher publisher;
	private final double threshold;
	private final int topK;

	public AnalyzeSimilarity(SubmissionGateway submissions, SimilarityStore store, EmbeddingPort embedder,
			EventPublisher publisher,
			@Value("${ms5.similarity.threshold:0.85}") double threshold,
			@Value("${ms5.similarity.top-k:10}") int topK) {
		this.submissions = submissions;
		this.store = store;
		this.embedder = embedder;
		this.publisher = publisher;
		this.threshold = threshold;
		this.topK = topK;
	}

	public SimilarityReport analyze(Request request) {
		if (request.submissionId() == null || request.challengeId() == null) {
			throw new IllegalArgumentException("submissionId et challengeId sont obligatoires");
		}
		SubmissionGateway.Snapshot snapshot = submissions.find(request.submissionId())
				.orElseThrow(() -> new NotFoundException("Soumission introuvable : " + request.submissionId()));

		// S8 : le code sera cloné au commit figé et chunké ; ici la source est déjà déterministe.
		String source = snapshot.repoUrl() + "|" + snapshot.commitSha() + "|" + snapshot.branch();
		double[] vector = embedder.embed(source);
		String modelVersion = embedder.modelVersion();
		String chunkId = tn.arene.ms5.domain.Verification.fingerprint(source, modelVersion);
		store.saveEmbedding(request.submissionId(), request.challengeId(), chunkId, "source", chunkId,
				modelVersion, vector);

		Map<UUID, Double> scores = store.scoresForChallenge(request.challengeId(), request.submissionId(),
				vector, modelVersion, topK);
		java.util.List<SimilarityMatch> matches = SimilarityPolicy.topK(scores, topK);
		SimilarityReport report = SimilarityReport.of(request.submissionId(), request.challengeId(),
				matches, threshold, modelVersion);
		store.save(report, threshold);

		if (report.isFlagged()) {
			publisher.publish("similarity.flagged", report.getId(), Map.of(
					"reportId", report.getId().toString(),
					"submissionId", report.getSubmissionId().toString(),
					"challengeId", report.getChallengeId().toString(),
					"maxSimilarity", report.getMaxSimilarity(),
					"threshold", threshold,
					"severity", SimilarityPolicy.severity(report.getMaxSimilarity()),
					"detectedAt", report.getAnalyzedAt().toString()));
		}
		return report;
	}

	public SimilarityReport latest(UUID submissionId) {
		return store.findLatest(submissionId)
				.orElseThrow(() -> new NotFoundException(
						"Aucun rapport de similarité pour cette soumission : " + submissionId));
	}
}
