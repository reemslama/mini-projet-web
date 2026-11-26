package tn.arene.ms5.application.port;

import java.util.List;
import java.util.UUID;

import tn.arene.ms5.domain.TestResult;

/** Exécution du runner épinglé, sans réseau et sans stderr non redacté. */
public interface SandboxPort {

	record Run(List<TestResult> tests, Double coverage, long durationMs, String logs, String image) {
	}

	Run execute(UUID verificationId, String repoUrl, String commitSha, List<String> command);
}
