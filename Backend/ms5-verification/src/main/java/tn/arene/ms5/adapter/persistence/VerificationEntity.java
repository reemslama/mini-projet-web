package tn.arene.ms5.adapter.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import tn.arene.ms5.domain.Verification;
import tn.arene.ms5.domain.VerificationStatus;
import tn.arene.ms5.domain.VerdictResult;

/** Miroir JPA du domaine ; aucun champ interne n'est exposé dans l'API. */
@Entity
@Table(name = "verifications")
public class VerificationEntity {

	@Id
	@Column(name = "id")
	private UUID id;

	@Column(name = "submission_id", nullable = false)
	private UUID submissionId;

	@Column(name = "challenge_id")
	private UUID challengeId;

	@Column(name = "applicant_id")
	private UUID applicantId;

	@Column(name = "repo_url", nullable = false)
	private String repoUrl;

	@Column(name = "commit_sha", nullable = false)
	private String commitSha;

	@Column(name = "branch")
	private String branch;

	@Column(name = "test_command")
	private String testCommand;

	@Column(name = "spec_digest")
	private String specDigest;

	@Column(name = "idempotency_key", nullable = false)
	private String idempotencyKey;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private VerificationStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "result")
	private VerdictResult result;

	@Column(name = "passed", nullable = false)
	private int passed;

	@Column(name = "failed", nullable = false)
	private int failed;

	@Column(name = "total", nullable = false)
	private int total;

	@Column(name = "coverage")
	private Double coverage;

	@Column(name = "duration_ms", nullable = false)
	private long durationMs;

	@Column(name = "sandbox_image")
	private String sandboxImage;

	@Column(name = "error_code")
	private String errorCode;

	@Column(name = "error_message")
	private String errorMessage;

	@Column(name = "attempt", nullable = false)
	private int attempt;

	@Lob
	@Column(name = "logs")
	private String logs;

	@Column(name = "requested_by")
	private String requestedBy;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "started_at")
	private LocalDateTime startedAt;

	@Column(name = "finished_at")
	private LocalDateTime finishedAt;

	public static VerificationEntity from(Verification v) {
		VerificationEntity e = new VerificationEntity();
		e.id = v.getId();
		e.submissionId = v.getSubmissionId();
		e.challengeId = v.getChallengeId();
		e.applicantId = v.getApplicantId();
		e.repoUrl = v.getRepoUrl();
		e.commitSha = v.getCommitSha();
		e.branch = v.getBranch();
		e.testCommand = v.getTestCommand();
		e.specDigest = v.getSpecDigest();
		e.idempotencyKey = v.getIdempotencyKey();
		e.status = v.getStatus();
		e.result = v.getResult();
		e.passed = v.getPassed();
		e.failed = v.getFailed();
		e.total = v.getTotal();
		e.coverage = v.getCoverage();
		e.durationMs = v.getDurationMs();
		e.sandboxImage = v.getSandboxImage();
		e.errorCode = v.getErrorCode();
		e.errorMessage = v.getErrorMessage();
		e.attempt = v.getAttempt();
		e.logs = v.getLogs();
		e.requestedBy = v.getRequestedBy();
		e.createdAt = v.getCreatedAt();
		e.startedAt = v.getStartedAt();
		e.finishedAt = v.getFinishedAt();
		return e;
	}

	public Verification toDomain() {
		Verification v = new Verification();
		v.setId(id);
		v.setSubmissionId(submissionId);
		v.setChallengeId(challengeId);
		v.setApplicantId(applicantId);
		v.setRepoUrl(repoUrl);
		v.setCommitSha(commitSha);
		v.setBranch(branch);
		v.setTestCommand(testCommand);
		v.setSpecDigest(specDigest);
		v.setIdempotencyKey(idempotencyKey);
		v.setStatus(status);
		v.setResult(result);
		v.setPassed(passed);
		v.setFailed(failed);
		v.setTotal(total);
		v.setCoverage(coverage);
		v.setDurationMs(durationMs);
		v.setSandboxImage(sandboxImage);
		v.setErrorCode(errorCode);
		v.setErrorMessage(errorMessage);
		v.setAttempt(attempt);
		v.setLogs(logs);
		v.setRequestedBy(requestedBy);
		v.setCreatedAt(createdAt);
		v.setStartedAt(startedAt);
		v.setFinishedAt(finishedAt);
		return v;
	}

	public UUID getId() { return id; }
	public void setId(UUID id) { this.id = id; }
	public VerificationStatus getStatus() { return status; }
	public void setStatus(VerificationStatus status) { this.status = status; }
	public String getIdempotencyKey() { return idempotencyKey; }
	public void setIdempotencyKey(String key) { this.idempotencyKey = key; }
}
