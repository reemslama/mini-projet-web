package tn.arene.soumission.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import tn.arene.soumission.entities.Submission;
import tn.arene.soumission.entities.SubmissionStatus;

/** Projection API d'une soumission : aucune entité JPA n'est exposée. */
public record SubmissionResponse(
		UUID id,
		UUID challengeId,
		UUID applicantId,
		String repoUrl,
		String branch,
		SubmissionStatus status,
		String commitSha,
		LocalDateTime submittedAt) {

	public static SubmissionResponse from(Submission submission) {
		return new SubmissionResponse(
				submission.getId(),
				submission.getChallengeId(),
				submission.getApplicantId(),
				submission.getRepoUrl(),
				submission.getBranch(),
				submission.getStatus(),
				submission.getCommitSha(),
				submission.getSubmittedAt());
	}
}
