package tn.arene.ms5.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/** Aggregate racine MS5 : machine à états QUEUED → RUNNING → COMPLETED / ERROR. */
public class Verification {

	private UUID id;
	private UUID submissionId;
	private UUID challengeId;
	private UUID applicantId;
	private String repoUrl;
	private String commitSha;
	private String branch;
	private String testCommand;
	private String specDigest;
	private String idempotencyKey;
	private VerificationStatus status;
	private VerdictResult result;
	private int passed;
	private int failed;
	private int total;
	private Double coverage;
	private long durationMs;
	private String sandboxImage;
	private String errorCode;
	private String errorMessage;
	private int attempt;
	private String logs;
	private String requestedBy;
	private LocalDateTime createdAt;
	private LocalDateTime startedAt;
	private LocalDateTime finishedAt;
	private List<TestResult> testResults = new ArrayList<>();

	public static Verification launch(UUID submissionId, UUID challengeId, UUID applicantId, String repoUrl,
			String commitSha, String branch, String testCommand, String specDigest, int attempt, String requestedBy) {
		Verification verification = new Verification();
		verification.id = UUID.randomUUID();
		verification.submissionId = submissionId;
		verification.challengeId = challengeId;
		verification.applicantId = applicantId;
		verification.repoUrl = repoUrl;
		verification.commitSha = commitSha;
		verification.branch = branch == null ? "main" : branch;
		verification.testCommand = testCommand;
		verification.specDigest = specDigest;
		verification.idempotencyKey = fingerprint(submissionId, challengeId, repoUrl, commitSha, testCommand,
				specDigest, attempt == 1 ? "AUTO" : "MANUAL");
		verification.status = VerificationStatus.QUEUED;
		verification.attempt = Math.max(1, attempt);
		verification.sandboxImage = "local-fixture@sha256:ms5-dev";
		verification.requestedBy = requestedBy;
		verification.createdAt = LocalDateTime.now();
		verification.logs = "";
		return verification;
	}

	public void start() {
		if (status != VerificationStatus.QUEUED) {
			throw new IllegalStateException("Seule une verification QUEUED peut démarrer (état : " + status + ")");
		}
		status = VerificationStatus.RUNNING;
		startedAt = LocalDateTime.now();
	}

	/** Complète la vérification à partir des seuls résultats déterministes du runner. */
	public void complete(List<TestResult> results, Double rawCoverage, long runDurationMs, String runLogs) {
		if (status != VerificationStatus.RUNNING) {
			throw new IllegalStateException("Seule une verification RUNNING peut se terminer (état : " + status + ")");
		}
		testResults = results == null ? List.of() : List.copyOf(results);
		VerdictPolicy.Outcome outcome = VerdictPolicy.compute(testResults);
		result = outcome.result();
		passed = outcome.passed();
		failed = outcome.failed();
		total = outcome.total();
		coverage = VerdictPolicy.sanitizeCoverage(rawCoverage);
		durationMs = Math.max(0, runDurationMs);
		logs = runLogs == null ? "" : runLogs;
		status = VerificationStatus.COMPLETED;
		finishedAt = LocalDateTime.now();
	}

	/** ERROR ≠ FAIL : aucune donnée déterministe, donc aucun verdict à publier. */
	public void fail(String code, String message) {
		if (status != VerificationStatus.QUEUED && status != VerificationStatus.RUNNING) {
			throw new IllegalStateException("État terminal : " + status);
		}
		status = VerificationStatus.ERROR;
		errorCode = code;
		errorMessage = message;
		result = null;
		finishedAt = LocalDateTime.now();
	}

	public boolean isTerminal() {
		return status == VerificationStatus.COMPLETED || status == VerificationStatus.ERROR;
	}

	public boolean canPublishVerdict() {
		return status == VerificationStatus.COMPLETED && result != null && submissionId != null;
	}

	public double successRate() {
		return VerdictPolicy.successRate(passed, total);
	}

	public static String fingerprint(Object... parts) {
		StringBuilder canonical = new StringBuilder();
		for (Object part : parts) {
			canonical.append(part == null ? "" : part).append('\u001f');
		}
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 indisponible", exception);
		}
	}

	public UUID getId() { return id; }
	public void setId(UUID id) { this.id = id; }
	public UUID getSubmissionId() { return submissionId; }
	public void setSubmissionId(UUID submissionId) { this.submissionId = submissionId; }
	public UUID getChallengeId() { return challengeId; }
	public void setChallengeId(UUID challengeId) { this.challengeId = challengeId; }
	public UUID getApplicantId() { return applicantId; }
	public void setApplicantId(UUID applicantId) { this.applicantId = applicantId; }
	public String getRepoUrl() { return repoUrl; }
	public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }
	public String getCommitSha() { return commitSha; }
	public void setCommitSha(String commitSha) { this.commitSha = commitSha; }
	public String getBranch() { return branch; }
	public void setBranch(String branch) { this.branch = branch; }
	public String getTestCommand() { return testCommand; }
	public void setTestCommand(String testCommand) { this.testCommand = testCommand; }
	public String getSpecDigest() { return specDigest; }
	public void setSpecDigest(String specDigest) { this.specDigest = specDigest; }
	public String getIdempotencyKey() { return idempotencyKey; }
	public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
	public VerificationStatus getStatus() { return status; }
	public void setStatus(VerificationStatus status) { this.status = status; }
	public VerdictResult getResult() { return result; }
	public void setResult(VerdictResult result) { this.result = result; }
	public int getPassed() { return passed; }
	public void setPassed(int passed) { this.passed = passed; }
	public int getFailed() { return failed; }
	public void setFailed(int failed) { this.failed = failed; }
	public int getTotal() { return total; }
	public void setTotal(int total) { this.total = total; }
	public Double getCoverage() { return coverage; }
	public void setCoverage(Double coverage) { this.coverage = coverage; }
	public long getDurationMs() { return durationMs; }
	public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
	public String getSandboxImage() { return sandboxImage; }
	public void setSandboxImage(String sandboxImage) { this.sandboxImage = sandboxImage; }
	public String getErrorCode() { return errorCode; }
	public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
	public String getErrorMessage() { return errorMessage; }
	public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
	public int getAttempt() { return attempt; }
	public void setAttempt(int attempt) { this.attempt = attempt; }
	public String getLogs() { return logs; }
	public void setLogs(String logs) { this.logs = logs; }
	public String getRequestedBy() { return requestedBy; }
	public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getStartedAt() { return startedAt; }
	public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
	public LocalDateTime getFinishedAt() { return finishedAt; }
	public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
	public List<TestResult> getTestResults() { return testResults; }
	public void setTestResults(List<TestResult> testResults) { this.testResults = testResults; }
}
