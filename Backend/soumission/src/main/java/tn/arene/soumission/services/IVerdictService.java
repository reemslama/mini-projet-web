package tn.arene.soumission.services;

import java.util.UUID;

import tn.arene.soumission.dto.VerdictRequest;
import tn.arene.soumission.dto.VerdictResponse;

/** Cas d'usage MS3 autour du verdict de vérification. */
public interface IVerdictService {

	/** Dépose (ou remplace) le verdict renvoyé par MS5. */
	VerdictResponse deposit(UUID submissionId, VerdictRequest request);

	VerdictResponse findBySubmissionId(UUID submissionId);
}
