package tn.arene.ms5.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Rapport exposé par le contrat gelé ; aucun score n'affecte le verdict MS3. */
public class SimilarityReport {

	private UUID id;
	private UUID submissionId;
	private UUID challengeId;
	private double maxSimilarity;
	private boolean flagged;
	private double threshold;
	private String modelVersion;
	private List<SimilarityMatch> matches = List.of();
	private LocalDateTime analyzedAt;

	public static SimilarityReport of(UUID submissionId, UUID challengeId, List<SimilarityMatch> matches,
			double threshold, String modelVersion) {
		SimilarityReport report = new SimilarityReport();
		report.id = UUID.randomUUID();
		report.submissionId = submissionId;
		report.challengeId = challengeId;
		report.threshold = threshold;
		report.modelVersion = modelVersion;
		report.matches = matches == null ? List.of() : List.copyOf(matches);
		report.maxSimilarity = report.matches.stream().mapToDouble(SimilarityMatch::similarity).max().orElse(0);
		report.flagged = SimilarityPolicy.flagged(report.maxSimilarity, threshold);
		report.analyzedAt = LocalDateTime.now();
		return report;
	}

	public UUID getId() { return id; }
	public void setId(UUID id) { this.id = id; }
	public UUID getSubmissionId() { return submissionId; }
	public void setSubmissionId(UUID submissionId) { this.submissionId = submissionId; }
	public UUID getChallengeId() { return challengeId; }
	public void setChallengeId(UUID challengeId) { this.challengeId = challengeId; }
	public double getMaxSimilarity() { return maxSimilarity; }
	public void setMaxSimilarity(double maxSimilarity) { this.maxSimilarity = maxSimilarity; }
	public boolean isFlagged() { return flagged; }
	public void setFlagged(boolean flagged) { this.flagged = flagged; }
	public double getThreshold() { return threshold; }
	public void setThreshold(double threshold) { this.threshold = threshold; }
	public String getModelVersion() { return modelVersion; }
	public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
	public List<SimilarityMatch> getMatches() { return matches; }
	public void setMatches(List<SimilarityMatch> matches) { this.matches = matches; }
	public LocalDateTime getAnalyzedAt() { return analyzedAt; }
	public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
}
