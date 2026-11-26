package tn.arene.soumission.dto;

import java.util.UUID;

/** Corps de POST /api/submissions (création d'un brouillon). */
public record SubmissionCreateRequest(
		UUID challengeId,
		UUID applicantId,
		String repoUrl,
		String branch) {
}
