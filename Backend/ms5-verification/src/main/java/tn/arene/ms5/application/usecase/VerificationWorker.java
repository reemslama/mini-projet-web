package tn.arene.ms5.application.usecase;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import tn.arene.ms5.application.port.EventPublisher;
import tn.arene.ms5.application.port.SandboxPort;
import tn.arene.ms5.application.port.VerificationStore;
import tn.arene.ms5.domain.LogRedactor;
import tn.arene.ms5.domain.Verification;

/** File bornée relue depuis la table : reprise des QUEUED sans perte ni double exécution. */
@Service
public class VerificationWorker {

	private static final Logger log = LoggerFactory.getLogger(VerificationWorker.class);

	private final VerificationStore store;
	private final SandboxPort sandbox;
	private final EventPublisher publisher;
	private final String publicBaseUrl;

	public VerificationWorker(VerificationStore store, SandboxPort sandbox, EventPublisher publisher,
			@Value("${ms5.public-base-url:http://localhost:8080}") String publicBaseUrl) {
		this.store = store;
		this.sandbox = sandbox;
		this.publisher = publisher;
		this.publicBaseUrl = publicBaseUrl;
	}

	@Scheduled(initialDelayString = "${ms5.worker.initial-delay-ms:2000}",
			fixedDelayString = "${ms5.worker.poll-ms:1000}")
	public void poll() {
		for (Verification queued : store.findQueued(1)) {
			run(queued);
		}
	}

	/** Exécution d'un job : ERROR uniquement si le runner est indisponible. */
	public void run(Verification verification) {
		UUID id = verification.getId();
		try {
			verification.start();
			SandboxPort.Run run = sandbox.execute(id, verification.getRepoUrl(), verification.getCommitSha(),
					java.util.List.of(verification.getTestCommand().split("\\s+")));
			verification.complete(run.tests(), run.coverage(), run.durationMs(), LogRedactor.redact(run.logs()));
			verification.setSandboxImage(run.image());
			store.save(verification);
			if (verification.canPublishVerdict()) {
				publisher.publish("verification.completed", id, Map.ofEntries(
						Map.entry("verificationId", id.toString()),
						Map.entry("submissionId", verification.getSubmissionId().toString()),
						Map.entry("challengeId", String.valueOf(verification.getChallengeId())),
						Map.entry("commitSha", verification.getCommitSha()),
						Map.entry("result", verification.getResult().name()),
						Map.entry("passed", verification.getPassed()),
						Map.entry("failed", verification.getFailed()),
						Map.entry("total", verification.getTotal()),
						Map.entry("coverage", verification.getCoverage() == null ? "" : verification.getCoverage()),
						Map.entry("logsUrl", publicBaseUrl + "/ms5/api/verifications/" + id + "/logs"),
						Map.entry("verifiedAt", verification.getFinishedAt().toString()),
						Map.entry("source", "AUTO")));
			}
			log.info("verification {} → {} ({} tests)", id, verification.getStatus(), verification.getTotal());
		} catch (RuntimeException exception) {
			verification.fail("SANDBOX_UNAVAILABLE", exception.getMessage());
			store.save(verification);
			log.warn("verification {} en ERROR : {}", id, exception.getMessage());
		}
	}
}
