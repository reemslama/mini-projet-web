package tn.arene.soumission.entities;

/**
 * Cycle de vie d'une soumission : DRAFT → SUBMITTED → VERIFYING → PASSED / REJECTED / WITHDRAWN.
 */
public enum SubmissionStatus {
	DRAFT,
	SUBMITTED,
	VERIFYING,
	PASSED,
	REJECTED,
	WITHDRAWN
}
