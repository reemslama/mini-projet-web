package tn.arene.soumission.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import tn.arene.soumission.dto.SubmissionCreateRequest;
import tn.arene.soumission.dto.SubmissionResponse;
import tn.arene.soumission.entities.Submission;
import tn.arene.soumission.entities.SubmissionStatus;
import tn.arene.soumission.entities.VerdictResult;

/** Cas d'usage MS3 autour du cycle de vie des soumissions. */
public interface ISubmissionService {

	SubmissionResponse create(SubmissionCreateRequest request);

	List<SubmissionResponse> findByChallengeId(UUID challengeId, SubmissionStatus status);

	SubmissionResponse findById(UUID id);

	/**
	 * Soumet définitivement : fige le commit et contrôle la date limite.
	 * commitSha / deadline seront fournis par MS1 / MS5 lors de l'intégration ;
	 * pour l'instant ils sont transmis en paramètres par le contrôleur.
	 */
	SubmissionResponse submit(UUID id, String commitSha, LocalDateTime deadline);

	SubmissionResponse markVerifying(UUID id);

	SubmissionResponse withdraw(UUID id);

	/** Clôture la soumission selon le verdict déposé par MS5. */
	SubmissionResponse applyVerdict(UUID id, VerdictResult result);

	/** Accès interne pour les autres services : ne jamais exposer depuis un contrôleur. */
	Submission requireEntity(UUID id);
}
