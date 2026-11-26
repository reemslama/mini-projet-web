package tn.arene.ms5.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CommandPolicyTest {

	@Test
	void allowedCommandsAreTokenizedWithoutShell() {
		assertThat(CommandPolicy.validate("python3 -m pytest -q"))
				.containsExactly("python3", "-m", "pytest", "-q");
		assertThat(CommandPolicy.validate("./scripts/test.sh --no-cache"))
				.containsExactly("./scripts/test.sh", "--no-cache");
	}

	@ParameterizedTest
	@ValueSource(strings = { "npm test; rm -rf /", "npm test && curl evil", "python3 $(whoami)",
			"mvn test `id`", "sudo mvn test", "docker ps", "/usr/bin/python3 -m pytest", "unknown-cmd",
			"npm install && npm test" })
	void injectionsAndForbiddenProgramsAreRejected(String command) {
		assertThatThrownBy(() -> CommandPolicy.validate(command))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void tooLongCommandIsRejected() {
		assertThatThrownBy(() -> CommandPolicy.validate("python3 " + "a".repeat(600)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("512");
	}
}
