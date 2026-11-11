package com.enlice.submissions.dto;

import com.enlice.submissions.domain.Submission;
import com.enlice.submissions.domain.SubmissionStatus;
import java.time.Instant;
import java.util.UUID;

public record SubmissionResponse(
        UUID id,
        UUID challengeId,
        UUID applicantId,
        String repoUrl,
        String branch,
        SubmissionStatus status,
        String commitSha,
        Instant submittedAt) {

    public static SubmissionResponse from(Submission submission) {
        return new SubmissionResponse(submission.getId(), submission.getChallengeId(), submission.getApplicantId(),
                submission.getRepoUrl(), submission.getBranch(), submission.getStatus(), submission.getCommitSha(),
                submission.getSubmittedAt());
    }
}
