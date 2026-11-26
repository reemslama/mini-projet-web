package tn.arene.soumission.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import tn.arene.soumission.entities.Verdict;
import tn.arene.soumission.entities.VerdictResult;

/** Projection API d'un verdict : aucune entité JPA n'est exposée. */
public record VerdictResponse(
		UUID submissionId,
		VerdictResult result,
		int passed,
		int failed,
		int total,
		Double coverage,
		String logsUrl,
		LocalDateTime verifiedAt) {

	public static VerdictResponse from(Verdict verdict) {
		return new VerdictResponse(
				verdict.getSubmissionId(),
				verdict.getResult(),
				verdict.getPassed(),
				verdict.getFailed(),
				verdict.getTotal(),
				verdict.getCoverage(),
				verdict.getLogsUrl(),
				verdict.getVerifiedAt());
	}
}
