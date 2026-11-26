package tn.arene.soumission.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import tn.arene.soumission.exceptions.ConflictException;

/** Règles métier du cycle de vie : figer le commit, deadline, transitions, retrait. */
class SubmissionTest {

	private static final UUID CHALLENGE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID APPLICANT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

	private Submission draft() {
		return Submission.create(CHALLENGE_ID, APPLICANT_ID, "https://github.com/example/solution.git", "main");
	}

	private LocalDateTime futureDeadline() {
		return LocalDateTime.now().plusDays(1);
	}

	@Test
	void createInitializesDraftWithDefaultBranch() {
		Submission submission = Submission.create(
				CHALLENGE_ID, APPLICANT_ID, "https://github.com/example/solution.git", null);

		assertThat(submission.getChallengeId()).isEqualTo(CHALLENGE_ID);
		assertThat(submission.getApplicantId()).isEqualTo(APPLICANT_ID);
		assertThat(submission.getRepoUrl()).isEqualTo("https://github.com/example/solution.git");
		assertThat(submission.getBranch()).isEqualTo("main");
		assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.DRAFT);
		assertThat(submission.getCommitSha()).isNull();
		assertThat(submission.getSubmittedAt()).isNull();
	}

	@Test
	void createKeepsProvidedBranch() {
		assertThat(draft().getBranch()).isEqualTo("main");
		assertThat(Submission.create(CHALLENGE_ID, APPLICANT_ID, "url", "feature/x").getBranch())
				.isEqualTo("feature/x");
	}

	@Test
	void createRejectsMissingRequiredFields() {
		assertThatThrownBy(() -> Submission.create(null, APPLICANT_ID, "url", "main"))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> Submission.create(CHALLENGE_ID, null, "url", "main"))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> Submission.create(CHALLENGE_ID, APPLICANT_ID, "  ", "main"))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void submitFreezesCommitAndTimestampsTheSubmission() {
		Submission submission = draft();
		LocalDateTime before = LocalDateTime.now();

		submission.submit("abc123", futureDeadline());

		assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
		assertThat(submission.getCommitSha()).isEqualTo("abc123");
		assertThat(submission.getSubmittedAt()).isBetween(before, LocalDateTime.now());
	}

	@Test
	void submitRejectsPastDeadlineAndLeavesDraftUntouched() {
		Submission submission = draft();

		assertThatThrownBy(() -> submission.submit("abc123", LocalDateTime.now().minusDays(1)))
				.isInstanceOf(ConflictException.class)
				.hasMessageContaining("Date limite dépassée");
		assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.DRAFT);
		assertThat(submission.getCommitSha()).isNull();
	}

	@Test
	void submitTwiceIsRejected() {
		Submission submission = draft();
		submission.submit("abc123", futureDeadline());

		assertThatThrownBy(() -> submission.submit("def456", futureDeadline()))
				.isInstanceOf(ConflictException.class);
		assertThat(submission.getCommitSha()).isEqualTo("abc123");
	}

	@Test
	void freezeCommitRejectsBlankSha() {
		assertThatThrownBy(() -> draft().freezeCommit("  "))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("commit SHA");
	}

	@Test
	void frozenCommitCannotBeChanged() {
		Submission submission = draft();
		submission.freezeCommit("abc123");

		submission.freezeCommit("abc123"); // idempotent
		assertThatThrownBy(() -> submission.freezeCommit("def456"))
				.isInstanceOf(ConflictException.class)
				.hasMessageContaining("déjà figé");
		assertThat(submission.getCommitSha()).isEqualTo("abc123");
	}

	@Test
	void markVerifyingIsOnlyAllowedFromSubmitted() {
		Submission draft = draft();
		assertThatThrownBy(draft::markVerifying).isInstanceOf(ConflictException.class);

		Submission submitted = draft();
		submitted.submit("abc123", futureDeadline());
		submitted.markVerifying();
		assertThat(submitted.getStatus()).isEqualTo(SubmissionStatus.VERIFYING);
	}

	@Test
	void withdrawIsAllowedUntilTheSubmissionIsClosed() {
		Submission draft = draft();
		draft.withdraw();
		assertThat(draft.getStatus()).isEqualTo(SubmissionStatus.WITHDRAWN);

		Submission submitted = draft();
		submitted.submit("abc123", futureDeadline());
		submitted.withdraw();
		assertThat(submitted.getStatus()).isEqualTo(SubmissionStatus.WITHDRAWN);
	}

	@Test
	void withdrawIsRejectedForClosedSubmissions() {
		for (SubmissionStatus status : List.of(SubmissionStatus.PASSED, SubmissionStatus.REJECTED,
				SubmissionStatus.WITHDRAWN)) {
			Submission submission = draft();
			submission.setStatus(status);
			assertThatThrownBy(submission::withdraw).isInstanceOf(ConflictException.class);
		}
	}

	@Test
	void canBeJudgedRequiresFrozenCommitAndVerificationStatus() {
		Submission submission = draft();
		assertThat(submission.canBeJudged()).isFalse();

		submission.submit("abc123", futureDeadline());
		assertThat(submission.canBeJudged()).isFalse();

		submission.markVerifying();
		assertThat(submission.canBeJudged()).isTrue();

		submission.setStatus(SubmissionStatus.PASSED);
		assertThat(submission.canBeJudged()).isTrue();

		submission.setStatus(SubmissionStatus.REJECTED);
		assertThat(submission.canBeJudged()).isFalse();
	}
}
