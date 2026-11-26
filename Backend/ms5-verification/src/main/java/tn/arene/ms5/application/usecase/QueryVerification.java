package tn.arene.ms5.application.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import tn.arene.ms5.application.NotFoundException;
import tn.arene.ms5.application.port.VerificationStore;
import tn.arene.ms5.domain.Verification;

/** Lecture seule des vérifications et des logs redactés. */
@Service
public class QueryVerification {

	private final VerificationStore store;

	public QueryVerification(VerificationStore store) {
		this.store = store;
	}

	public Verification get(UUID id) {
		return store.findById(id)
				.orElseThrow(() -> new NotFoundException("Vérification introuvable : " + id));
	}

	public String logs(UUID id) {
		Verification verification = get(id);
		if (!verification.isTerminal()) {
			throw new NotFoundException("Aucun logs d'exécution pour la vérification : " + id);
		}
		return verification.getLogs() == null || verification.getLogs().isBlank()
				? "Aucun logs d'exécution.\n" : verification.getLogs();
	}

	public List<Verification> findBySubmission(UUID submissionId) {
		return store.findBySubmissionId(submissionId);
	}
}
