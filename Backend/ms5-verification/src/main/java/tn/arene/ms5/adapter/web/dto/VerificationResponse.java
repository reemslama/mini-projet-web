package tn.arene.ms5.adapter.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import tn.arene.ms5.domain.Verification;
import tn.arene.ms5.domain.VerificationStatus;
import tn.arene.ms5.domain.VerdictResult;

/** Exactement les 12 propriétés du schéma Verification du contrat gelé. */
public record VerificationResponse(UUID id, UUID submissionId, VerificationStatus status, VerdictResult result,
		int passed, int failed, int total, Double coverage, long durationMs, String sandboxImage,
		LocalDateTime startedAt, LocalDateTime finishedAt) {

	public static VerificationResponse from(Verification v) {
		return new VerificationResponse(v.getId(), v.getSubmissionId(), v.getStatus(), v.getResult(),
				v.getPassed(), v.getFailed(), v.getTotal(), v.getCoverage(), v.getDurationMs(),
				v.getSandboxImage(), v.getStartedAt(), v.getFinishedAt());
	}
}
