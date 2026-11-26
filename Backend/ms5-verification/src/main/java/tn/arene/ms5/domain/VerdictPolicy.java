package tn.arene.ms5.domain;

import java.util.Comparator;
import java.util.List;

/** Calcul pur du verdict : aucune I/O, aucune horloge, aucun appel IA. */
public final class VerdictPolicy {

	private VerdictPolicy() {
	}

	public record Outcome(VerdictResult result, int passed, int failed, int total) {
	}

	/** Tri stable puis comptage ; total = passed + failed ; zéro test prouvé ⇒ FAIL. */
	public static Outcome compute(List<TestResult> tests) {
		List<TestResult> sorted = tests == null ? List.of()
				: tests.stream().sorted(Comparator.comparing(TestResult::testName)).toList();
		int passed = (int) sorted.stream().filter(TestResult::isPassed).count();
		int failed = sorted.size() - passed;
		VerdictResult result = (!sorted.isEmpty() && failed == 0) ? VerdictResult.PASS : VerdictResult.FAIL;
		return new Outcome(result, passed, failed, sorted.size());
	}

	/** Une couverture hors [0,1], NaN ou absente n'influe pas sur le verdict. */
	public static Double sanitizeCoverage(Double coverage) {
		return coverage != null && Double.isFinite(coverage) && coverage >= 0 && coverage <= 1 ? coverage : null;
	}

	public static double successRate(int passed, int total) {
		return total == 0 ? 0 : (double) passed / total;
	}
}
