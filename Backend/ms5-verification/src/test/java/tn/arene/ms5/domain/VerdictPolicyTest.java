package tn.arene.ms5.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class VerdictPolicyTest {

	@Test
	void allPassingTestsGivePass() {
		VerdictPolicy.Outcome outcome = VerdictPolicy.compute(List.of(
				TestResult.pass("b", 1), TestResult.pass("a", 2)));
		assertThat(outcome.result()).isEqualTo(VerdictResult.PASS);
		assertThat(outcome.passed()).isEqualTo(2);
		assertThat(outcome.failed()).isZero();
		assertThat(outcome.total()).isEqualTo(2);
	}

	@Test
	void anyFailureGivesFailAndCountsAreConsistent() {
		VerdictPolicy.Outcome outcome = VerdictPolicy.compute(List.of(
				TestResult.pass("a", 1), TestResult.fail("b", 1, "boom")));
		assertThat(outcome.result()).isEqualTo(VerdictResult.FAIL);
		assertThat(outcome.passed() + outcome.failed()).isEqualTo(outcome.total());
	}

	@Test
	void zeroProvenTestIsFailNotError() {
		VerdictPolicy.Outcome outcome = VerdictPolicy.compute(List.of());
		assertThat(outcome.result()).isEqualTo(VerdictResult.FAIL);
		assertThat(outcome.total()).isZero();
	}

	@Test
	void coverageOutOfRangeOrNaNIsDiscardedWithoutChangingVerdict() {
		assertThat(VerdictPolicy.sanitizeCoverage(-0.1)).isNull();
		assertThat(VerdictPolicy.sanitizeCoverage(1.4)).isNull();
		assertThat(VerdictPolicy.sanitizeCoverage(Double.NaN)).isNull();
		assertThat(VerdictPolicy.sanitizeCoverage(0.83)).isEqualTo(0.83);
	}

	@Test
	void successRateHandlesEmptyTotals() {
		assertThat(VerdictPolicy.successRate(0, 0)).isZero();
		assertThat(VerdictPolicy.successRate(3, 4)).isEqualTo(0.75);
	}
}
