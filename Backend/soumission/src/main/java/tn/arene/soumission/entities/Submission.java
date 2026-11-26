package tn.arene.soumission.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.arene.soumission.exceptions.ConflictException;

/** Dépôt soumis à un défi, figé à un commit. Les identifiants externes sont stockés en UUID. */
@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "challenge_id", nullable = false)
	private UUID challengeId;

	@Column(name = "applicant_id", nullable = false)
	private UUID applicantId;

	@Column(name = "repo_url", nullable = false)
	private String repoUrl;

	@Column(name = "branch_name")
	private String branch;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SubmissionStatus status = SubmissionStatus.DRAFT;

	@Column(name = "commit_sha")
	private String commitSha;

	@Column(name = "submitted_at")
	private LocalDateTime submittedAt;

	/** Crée un brouillon à partir des informations du dépôt. */
	public static Submission create(UUID challengeId, UUID applicantId, String repoUrl, String branch) {
		if (challengeId == null || applicantId == null || repoUrl == null || repoUrl.isBlank()) {
			throw new IllegalArgumentException("challengeId, applicantId et repoUrl sont obligatoires");
		}
		Submission submission = new Submission();
		submission.setChallengeId(challengeId);
		submission.setApplicantId(applicantId);
		submission.setRepoUrl(repoUrl);
		submission.setBranch(branch == null || branch.isBlank() ? "main" : branch);
		submission.setStatus(SubmissionStatus.DRAFT);
		return submission;
	}

	/** Fige le commit et clôture la soumission ; refuse une date limite dépassée (409). */
	public Submission submit(String commitSha, LocalDateTime deadline) {
		if (status != SubmissionStatus.DRAFT) {
			throw new ConflictException("Soumission déjà figée ou clôturée");
		}
		if (isLate(deadline)) {
			throw new ConflictException("Date limite dépassée ou soumission déjà figée");
		}
		freezeCommit(commitSha);
		status = SubmissionStatus.SUBMITTED;
		submittedAt = LocalDateTime.now();
		return this;
	}

	/** Fige le SHA du dépôt : une fois posé, il ne peut plus être modifié (ADR-005). */
	public void freezeCommit(String commitSha) {
		if (commitSha == null || commitSha.isBlank()) {
			throw new IllegalArgumentException("Un commit SHA est requis pour figer la soumission");
		}
		if (this.commitSha != null && !this.commitSha.equals(commitSha)) {
			throw new ConflictException("Le commit est déjà figé");
		}
		this.commitSha = commitSha;
	}

	/** Passe en vérification après l'acceptation de la soumission. */
	public void markVerifying() {
		if (status != SubmissionStatus.SUBMITTED) {
			throw new ConflictException("Seule une soumission SOUMISE peut passer en vérification");
		}
		status = SubmissionStatus.VERIFYING;
	}

	public boolean isLate(LocalDateTime deadline) {
		return deadline != null && LocalDateTime.now().isAfter(deadline);
	}

	/** Retrait possible tant que la soumission n'est pas clôturée. */
	public void withdraw() {
		if (status == SubmissionStatus.PASSED || status == SubmissionStatus.REJECTED
				|| status == SubmissionStatus.WITHDRAWN) {
			throw new ConflictException("Soumission déjà clôturée, retrait impossible");
		}
		status = SubmissionStatus.WITHDRAWN;
	}

	/** Une soumission est notable si son commit est figé et si elle est en vérification ou retenue. */
	public boolean canBeJudged() {
		return commitSha != null
				&& (status == SubmissionStatus.VERIFYING || status == SubmissionStatus.PASSED);
	}
}
