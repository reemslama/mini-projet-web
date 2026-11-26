package tn.arene.ms5.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tn.arene.ms5.domain.Verification;
import tn.arene.ms5.domain.VerificationStatus;

/** Port de persistance du domaine (aucune annotation JPA ici). */
public interface VerificationStore {

	Verification save(Verification verification);

	Optional<Verification> findById(UUID id);

	Optional<Verification> findActiveByIdempotencyKey(String key);

	List<Verification> findBySubmissionId(UUID submissionId);

	/** File bornée relue depuis la table au démarrage : aucun job perdu. */
	List<Verification> findQueued(int limit);
}
