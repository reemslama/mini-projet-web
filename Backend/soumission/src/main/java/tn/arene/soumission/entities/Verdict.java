package tn.arene.soumission.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Verdict déterministe renvoyé par MS5 pour une soumission. */
@Entity
@Table(name = "verdicts")
@Getter
@Setter
@NoArgsConstructor
public class Verdict {

	/** Clé primaire = identifiant de la soumission (verdict 0..1 par soumission). */
	@Id
	@Column(name = "submission_id")
	private UUID submissionId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private VerdictResult result;

	@Column(nullable = false)
	private int passed;

	@Column(nullable = false)
	private int failed;

	@Column(nullable = false)
	private int total;

	/** Couverture éventuellement indisponible. */
	private Double coverage;

	@Column(name = "logs_url")
	private String logsUrl;

	@Column(name = "verified_at")
	private LocalDateTime verifiedAt;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "verdict_test_results", joinColumns = @JoinColumn(name = "submission_id"))
	private List<TestCaseResult> testResults = new ArrayList<>();

	public boolean isPass() {
		return result == VerdictResult.PASS;
	}

	public double successRate() {
		return total == 0 ? 0.0 : (double) passed / total;
	}

	/** Fabrique un verdict à partir du résultat de vérification ; passed + failed = total. */
	public static Verdict fromVerification(UUID submissionId, VerdictResult result,
			int passed, int failed, Double coverage, String logsUrl,
			List<TestCaseResult> testResults) {
		if (submissionId == null || result == null || passed < 0 || failed < 0) {
			throw new IllegalArgumentException("Verdict invalide : identifiant, résultat et comptages requis");
		}
		Verdict verdict = new Verdict();
		verdict.setSubmissionId(submissionId);
		verdict.setResult(result);
		verdict.setPassed(passed);
		verdict.setFailed(failed);
		verdict.setTotal(passed + failed);
		verdict.setCoverage(coverage);
		verdict.setLogsUrl(logsUrl);
		verdict.setVerifiedAt(LocalDateTime.now());
		verdict.setTestResults(testResults == null ? new ArrayList<>() : new ArrayList<>(testResults));
		return verdict;
	}

	public String summary() {
		StringBuilder message = new StringBuilder()
				.append(result)
				.append(" — ")
				.append(passed)
				.append('/')
				.append(total)
				.append(" tests réussis");
		if (coverage != null) {
			message.append(String.format(Locale.ROOT, " (couverture %.1f%%)", coverage));
		}
		return message.toString();
	}
}
