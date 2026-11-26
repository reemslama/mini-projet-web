package tn.arene.ms5.domain;

/** Résultat d'un test d'acceptation, sans horodatage afin de rester déterministe. */
public record TestResult(String testName, VerdictResult outcome, long durationMs, String logs) {

	public static TestResult pass(String name, long durationMs) {
		return new TestResult(name, VerdictResult.PASS, durationMs, "");
	}

	public static TestResult fail(String name, long durationMs, String logs) {
		return new TestResult(name, VerdictResult.FAIL, durationMs, logs == null ? "" : logs);
	}

	public boolean isPassed() {
		return outcome == VerdictResult.PASS;
	}
}
