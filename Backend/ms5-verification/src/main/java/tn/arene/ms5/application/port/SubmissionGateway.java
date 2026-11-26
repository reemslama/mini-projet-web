package tn.arene.ms5.application.port;

import java.util.Optional;
import java.util.UUID;

/** Métadonnées de soumission : MS3 fait foi pour le commit figé (ADR-005). */
public interface SubmissionGateway {

	record Snapshot(UUID submissionId, UUID challengeId, UUID applicantId, String repoUrl,
			String branch, String commitSha) {
	}

	Optional<Snapshot> find(UUID submissionId);
}
