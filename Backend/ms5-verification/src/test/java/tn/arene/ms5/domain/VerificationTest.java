package tn.arene.ms5.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class VerificationTest {

	private Verification queued() {
		return Verification.launch(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
				"https://github.com/a/b.git", "abc123", "main", "python3 -m pytest -q", "digest", 1, "user:test");
	}

	@Test
	void launchInitializesQueuedWithDeterministicKey() {
		Verification verification = queued();
		assertThat(verification.getStatus()).isEqualTo(VerificationStatus.QUEUED);
		assertThat(verification.getResult()).isNull();
		assertThat(verification.getIdempotencyKey()).hasSize(64);
		assertThat(Verification.fingerprint("a", "b")).isEqualTo(Verification.fingerprint("a", "b"));
	}

	@Test
	void completeComputesDeterministicVerdict() {
		Verification verification = queued();
		verification.start();
		verification.complete(List.of(TestResult.pass("b", 2), TestResult.pass("a", 1)), 0.83, 3, "logs");

		assertThat(verification.getStatus()).isEqualTo(VerificationStatus.COMPLETED);
		assertThat(verification.getResult()).isEqualTo(VerdictResult.PASS);
		assertThat(verification.getTotal()).isEqualTo(2);
		assertThat(verification.getCoverage()).isEqualTo(0.83);
		assertThat(verification.canPublishVerdict()).isTrue();
	}

	@Test
	void failNeverInventsAVerdict() {
		Verification verification = queued();
		verification.fail("NO_ACCEPTANCE_CRITERIA", "critères vides");
		assertThat(verification.getStatus()).isEqualTo(VerificationStatus.ERROR);
		assertThat(verification.getResult()).isNull();
		assertThat(verification.canPublishVerdict()).isFalse();
	}

	@Test
	void terminalStatesRejectNewTransitions() {
		Verification verification = queued();
		verification.start();
		verification.complete(List.of(TestResult.fail("a", 1, "ko")), null, 1, "");
		assertThatThrownBy(verification::start).isInstanceOf(IllegalStateException.class);
		assertThat(verification.getResult()).isEqualTo(VerdictResult.FAIL);
	}

	@Test
	void startIsOnlyAllowedFromQueued() {
		Verification verification = queued();
		verification.start();
		assertThatThrownBy(verification::start).isInstanceOf(IllegalStateException.class);
	}
}
