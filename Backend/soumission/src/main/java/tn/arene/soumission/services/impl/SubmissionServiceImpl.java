package tn.arene.soumission.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tn.arene.soumission.dto.SubmissionCreateRequest;
import tn.arene.soumission.dto.SubmissionResponse;
import tn.arene.soumission.entities.Submission;
import tn.arene.soumission.entities.SubmissionStatus;
import tn.arene.soumission.entities.VerdictResult;
import tn.arene.soumission.exceptions.ResourceNotFoundException;
import tn.arene.soumission.repositories.SubmissionRepository;
import tn.arene.soumission.services.ISubmissionService;

@Service
@Transactional(readOnly = true)
public class SubmissionServiceImpl implements ISubmissionService {

	private final SubmissionRepository submissionRepository;

	public SubmissionServiceImpl(SubmissionRepository submissionRepository) {
		this.submissionRepository = submissionRepository;
	}

	@Override
	@Transactional
	public SubmissionResponse create(SubmissionCreateRequest request) {
		Submission submission = Submission.create(
				request.challengeId(), request.applicantId(), request.repoUrl(), request.branch());
		return SubmissionResponse.from(submissionRepository.save(submission));
	}

	@Override
	public List<SubmissionResponse> findByChallengeId(UUID challengeId, SubmissionStatus status) {
		List<Submission> submissions = status == null
				? submissionRepository.findByChallengeId(challengeId)
				: submissionRepository.findByChallengeIdAndStatus(challengeId, status);
		return submissions.stream().map(SubmissionResponse::from).toList();
	}

	@Override
	public SubmissionResponse findById(UUID id) {
		return SubmissionResponse.from(requireEntity(id));
	}

	@Override
	@Transactional
	public SubmissionResponse submit(UUID id, String commitSha, LocalDateTime deadline) {
		Submission submission = requireEntity(id);
		submission.submit(commitSha, deadline);
		return SubmissionResponse.from(submissionRepository.save(submission));
	}

	@Override
	@Transactional
	public SubmissionResponse markVerifying(UUID id) {
		Submission submission = requireEntity(id);
		submission.markVerifying();
		return SubmissionResponse.from(submissionRepository.save(submission));
	}

	@Override
	@Transactional
	public SubmissionResponse withdraw(UUID id) {
		Submission submission = requireEntity(id);
		submission.withdraw();
		return SubmissionResponse.from(submissionRepository.save(submission));
	}

	@Override
	@Transactional
	public SubmissionResponse applyVerdict(UUID id, VerdictResult result) {
		Submission submission = requireEntity(id);
		submission.setStatus(result == VerdictResult.PASS
				? SubmissionStatus.PASSED
				: SubmissionStatus.REJECTED);
		return SubmissionResponse.from(submissionRepository.save(submission));
	}

	@Override
	public Submission requireEntity(UUID id) {
		return submissionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Soumission introuvable : " + id));
	}
}
