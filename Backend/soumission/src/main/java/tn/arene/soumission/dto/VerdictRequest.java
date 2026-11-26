package tn.arene.soumission.dto;

import java.util.UUID;

import tn.arene.soumission.entities.VerdictResult;

/** Corps de PUT /api/submissions/{id}/verdict (appel interne de MS5). */
public record VerdictRequest(
		UUID submissionId,
		VerdictResult result,
		int passed,
		int failed,
		int total,
		Double coverage,
		String logsUrl) {
}
