package tn.arene.soumission.services.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tn.arene.soumission.dto.VerdictRequest;
import tn.arene.soumission.dto.VerdictResponse;
import tn.arene.soumission.entities.Submission;
import tn.arene.soumission.entities.SubmissionStatus;
import tn.arene.soumission.entities.Verdict;
import tn.arene.soumission.exceptions.ConflictException;
import tn.arene.soumission.exceptions.ResourceNotFoundException;
import tn.arene.soumission.repositories.VerdictRepository;
import tn.arene.soumission.services.ISubmissionService;
import tn.arene.soumission.services.IVerdictService;

@Service
@Transactional(readOnly = true)
public class VerdictServiceImpl implements IVerdictService {

	private final VerdictRepository verdictRepository;
	private final ISubmissionService submissionService;

	public VerdictServiceImpl(VerdictRepository verdictRepository, ISubmissionService submissionService) {
		this.verdictRepository = verdictRepository;
		this.submissionService = submissionService;
	}

	@Override
	@Transactional
	public VerdictResponse deposit(UUID submissionId, VerdictRequest request) {
		Submission submission = submissionService.requireEntity(submissionId);
		if (request.submissionId() != null && !request.submissionId().equals(submissionId)) {
			throw new ConflictException("L'identifiant du verdict ne correspond pas à l'URL");
		}
		if (request.result() == null || request.total() != request.passed() + request.failed()) {
			throw new ConflictException("passed + failed doit égaler total");
		}
		// Le dépôt du verdict par MS5 fait passer la soumission en vérification.
		if (submission.getStatus() == SubmissionStatus.SUBMITTED) {
			submissionService.markVerifying(submissionId);
		}
		if (!submission.canBeJudged()) {
			throw new ConflictException("La soumission n'est pas éligible à un verdict");
		}

		// Les détails par test restent internes : ils seront peuplés lors de l'intégration avec MS5.
		Verdict verdict = Verdict.fromVerification(
				submissionId,
				request.result(),
				request.passed(),
				request.failed(),
				request.coverage(),
				request.logsUrl(),
				null);
		Verdict saved = verdictRepository.save(verdict);
		submissionService.applyVerdict(submissionId, request.result());
		return VerdictResponse.from(saved);
	}

	@Override
	public VerdictResponse findBySubmissionId(UUID submissionId) {
		Verdict verdict = verdictRepository.findBySubmissionId(submissionId)
				.orElseThrow(() -> new ResourceNotFoundException("Verdict introuvable : " + submissionId));
		return VerdictResponse.from(verdict);
	}
}
