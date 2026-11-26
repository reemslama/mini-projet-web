package tn.arene.ms5.adapter.persistence;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

import tn.arene.ms5.domain.SimilarityMatch;
import tn.arene.ms5.domain.SimilarityReport;

@Entity
@Table(name = "similarity_reports")
public class SimilarityReportEntity {

	@Id
	@Column(name = "id")
	private UUID id;

	@Column(name = "submission_id", nullable = false)
	private UUID submissionId;

	@Column(name = "challenge_id", nullable = false)
	private UUID challengeId;

	@Column(name = "max_similarity", nullable = false)
	private double maxSimilarity;

	@Column(name = "flagged", nullable = false)
	private boolean flagged;

	@Column(name = "threshold", nullable = false)
	private double threshold;

	@Column(name = "model_version", nullable = false)
	private String modelVersion;

	@Column(name = "analyzed_at", nullable = false)
	private LocalDateTime analyzedAt;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "similarity_matches", joinColumns = @JoinColumn(name = "report_id"))
	@MapKeyColumn(name = "other_submission_id")
	@Column(name = "similarity")
	private Map<UUID, Double> matches = new HashMap<>();

	public static SimilarityReportEntity from(SimilarityReport report, double threshold) {
		SimilarityReportEntity entity = new SimilarityReportEntity();
		entity.id = report.getId();
		entity.submissionId = report.getSubmissionId();
		entity.challengeId = report.getChallengeId();
		entity.maxSimilarity = report.getMaxSimilarity();
		entity.flagged = report.isFlagged();
		entity.threshold = threshold;
		entity.modelVersion = report.getModelVersion();
		entity.analyzedAt = report.getAnalyzedAt();
		for (SimilarityMatch match : report.getMatches()) {
			entity.matches.put(match.otherSubmissionId(), match.similarity());
		}
		return entity;
	}

	public SimilarityReport toDomain() {
		SimilarityReport report = new SimilarityReport();
		report.setId(id);
		report.setSubmissionId(submissionId);
		report.setChallengeId(challengeId);
		report.setMaxSimilarity(maxSimilarity);
		report.setFlagged(flagged);
		report.setThreshold(threshold);
		report.setModelVersion(modelVersion);
		report.setAnalyzedAt(analyzedAt);
		report.setMatches(matches.entrySet().stream()
				.map(entry -> new SimilarityMatch(entry.getKey(), entry.getValue())).toList());
		return report;
	}

	public UUID getId() { return id; }
	public UUID getSubmissionId() { return submissionId; }
	public void setId(UUID id) { this.id = id; }
	public void setSubmissionId(UUID submissionId) { this.submissionId = submissionId; }
	public void setChallengeId(UUID challengeId) { this.challengeId = challengeId; }
	public void setMaxSimilarity(double maxSimilarity) { this.maxSimilarity = maxSimilarity; }
	public void setFlagged(boolean flagged) { this.flagged = flagged; }
	public void setThreshold(double threshold) { this.threshold = threshold; }
	public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
	public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
	public Map<UUID, Double> getMatches() { return matches; }
	public void setMatches(Map<UUID, Double> matches) { this.matches = matches; }
	public List<SimilarityMatch> matchesAsList() {
		return matches.entrySet().stream().map(e -> new SimilarityMatch(e.getKey(), e.getValue())).toList();
	}
}
