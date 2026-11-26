package tn.arene.ms5.adapter.web.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import tn.arene.ms5.domain.SimilarityMatch;
import tn.arene.ms5.domain.SimilarityReport;

/** Exactement les 6 propriétés du schéma SimilarityReport du contrat gelé. */
public record SimilarityReportResponse(UUID submissionId, UUID challengeId, double maxSimilarity,
		boolean flagged, List<Match> matches, LocalDateTime analyzedAt) {

	public record Match(UUID otherSubmissionId, double similarity) {
	}

	public static SimilarityReportResponse from(SimilarityReport report) {
		List<Match> matches = report.getMatches().stream()
				.map(match -> new Match(match.otherSubmissionId(), match.similarity())).toList();
		return new SimilarityReportResponse(report.getSubmissionId(), report.getChallengeId(),
				report.getMaxSimilarity(), report.isFlagged(), matches, report.getAnalyzedAt());
	}
}
