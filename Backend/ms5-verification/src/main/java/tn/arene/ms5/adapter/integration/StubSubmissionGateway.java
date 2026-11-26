package tn.arene.ms5.adapter.integration;

import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import tn.arene.ms5.application.port.SubmissionGateway;
import tn.arene.ms5.domain.Verification;

/** Enrichissement local pour démarrer MS5 sans MS1/MS3 ; MS3 fait foi dès que mode=feign. */
@Component
@ConditionalOnProperty(name = "ms5.enrichment.mode", havingValue = "stub", matchIfMissing = true)
public class StubSubmissionGateway implements SubmissionGateway {

	private static final UUID CHALLENGE = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID APPLICANT = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Override
	public Optional<Snapshot> find(UUID submissionId) {
		if (submissionId == null) return Optional.empty();
		String commit = Verification.fingerprint(submissionId).substring(0, 12);
		return Optional.of(new Snapshot(submissionId, CHALLENGE, APPLICANT,
				"https://github.com/arena/solution.git", "main", commit));
	}
}
