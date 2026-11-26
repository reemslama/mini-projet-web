package tn.arene.ms5.adapter.sandbox;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import tn.arene.ms5.application.port.SandboxPort;
import tn.arene.ms5.domain.CommandPolicy;
import tn.arene.ms5.domain.TestResult;

/**
 * runner déterministe local (mode développement/S5). Le runner Docker durci par digest est
 * branché en S7 via le même port ; le calcul du verdict reste identique car il ne dépend
 * que des TestResult parsés.
 */
@Component
@ConditionalOnProperty(name = "ms5.sandbox.mode", havingValue = "fixture", matchIfMissing = true)
public class LocalFixtureSandbox implements SandboxPort {

	private static final Pattern COVERAGE = Pattern.compile("\"lineCoverage\"\\s*:\\s*([0-9.]+)");
	private final String image;

	public LocalFixtureSandbox(@Value("${ms5.sandbox.image:local-fixture@sha256:ms5-dev}") String image) {
		this.image = image;
	}

	@Override
	public SandboxPort.Run execute(UUID verificationId, String repoUrl, String commitSha, List<String> command) {
		if (repoUrl == null || repoUrl.isBlank() || commitSha == null || commitSha.isBlank()) {
			throw new IllegalArgumentException("repoUrl et commitSha sont requis pour exécuter les tests");
		}
		CommandPolicy.validate(String.join(" ", command));
		String xml = read("sandbox/fixture-junit.xml");
		String coverageJson = read("sandbox/fixture-coverage.json");
		List<TestResult> tests = JUnitXmlParser.parse(xml);
		Matcher matcher = COVERAGE.matcher(coverageJson);
		Double coverage = matcher.find() ? Double.parseDouble(matcher.group(1)) : null;
		long duration = tests.stream().mapToLong(TestResult::durationMs).sum();
		String logs = "runner=local-fixture verification=" + verificationId
				+ " commit=" + commitSha + "\n" + xml;
		return new SandboxPort.Run(tests, coverage, duration, logs, image);
	}

	private static String read(String path) {
		try (var stream = new ClassPathResource(path).getInputStream()) {
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (Exception exception) {
			throw new IllegalStateException("Fixture runner indisponible : " + path, exception);
		}
	}
}
