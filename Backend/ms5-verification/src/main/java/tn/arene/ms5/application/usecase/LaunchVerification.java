package tn.arene.ms5.application.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import tn.arene.ms5.application.NotFoundException;
import tn.arene.ms5.application.port.SubmissionGateway;
import tn.arene.ms5.application.port.VerificationStore;
import tn.arene.ms5.domain.CommandPolicy;
import tn.arene.ms5.domain.Verification;

/** Use case 1 : création idempotente d'une verification QUEUED (POST /api/verifications). */
@Service
public class LaunchVerification {

	public record Request(UUID submissionId, UUID challengeId, String repoUrl, String commitSha,
			String testCommand, String requestedBy) {
	}

	private final VerificationStore store;
	private final SubmissionGateway submissions;

	public LaunchVerification(VerificationStore store, SubmissionGateway submissions) {
		this.store = store;
		this.submissions = submissions;
	}

	public Verification launch(Request request) {
		if (request.submissionId() == null) {
			throw new IllegalArgumentException("submissionId est obligatoire");
		}
		SubmissionGateway.Snapshot snapshot = submissions.find(request.submissionId())
				.orElseThrow(() -> new NotFoundException("Soumission introuvable : " + request.submissionId()));

		String command = request.testCommand() == null || request.testCommand().isBlank()
				? CommandPolicy.defaultCommand() : request.testCommand();
		CommandPolicy.validate(command);

		String specDigest = Verification.fingerprint(snapshot.challengeId(), command);
		Verification proposal = Verification.launch(
				request.submissionId(), snapshot.challengeId(), snapshot.applicantId(),
				snapshot.repoUrl(), snapshot.commitSha(), snapshot.branch(),
				command, specDigest, 1, request.requestedBy());

		Optional<Verification> active = store.findActiveByIdempotencyKey(proposal.getIdempotencyKey());
		if (active.isPresent()) {
			return active.get();
		}
		int attempt = 1 + store.findBySubmissionId(request.submissionId()).stream()
				.mapToInt(Verification::getAttempt).max().orElse(0);
		Verification verification = Verification.launch(
				request.submissionId(), snapshot.challengeId(), snapshot.applicantId(),
				snapshot.repoUrl(), snapshot.commitSha(), snapshot.branch(),
				command, specDigest, attempt, request.requestedBy());
		return store.save(verification);
	}
}
