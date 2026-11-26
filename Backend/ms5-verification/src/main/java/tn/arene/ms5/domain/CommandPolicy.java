package tn.arene.ms5.domain;

import java.util.List;
import java.util.Set;

/** Validation sans shell de testCommand (§ 5.3 du plan MS5). */
public final class CommandPolicy {

	private static final Set<String> ALLOWED = Set.of(
			"npm", "node", "mvn", "gradle", "gradlew", "python3", "pytest", "go", "cargo", "./scripts/test.sh");
	private static final Set<String> FORBIDDEN = Set.of(
			"sudo", "su", "curl", "wget", "ssh", "docker", "kubectl", "nc", "rm", "chmod", "mount");
	private static final String METACHARACTERS = ";|&$`<>\n\r\\";

	private CommandPolicy() {
	}

	/** Commande par défaut lorsque le corps ne la fournit pas (spec MS1 en production). */
	public static String defaultCommand() {
		return "python3 -m pytest -q";
	}

	/** Tokenise et valide ; lève IllegalArgumentException si la commande est refusée. */
	public static List<String> validate(String command) {
		if (command == null || command.isBlank()) {
			throw new IllegalArgumentException("testCommand est obligatoire");
		}
		String trimmed = command.trim();
		if (trimmed.length() > 512) {
			throw new IllegalArgumentException("testCommand dépasse 512 caractères");
		}
		for (char c : trimmed.toCharArray()) {
			if (METACHARACTERS.indexOf(c) >= 0) {
				throw new IllegalArgumentException("testCommand contient un métacaractère interdit : " + c);
			}
		}
		List<String> tokens = List.of(trimmed.split("\\s+"));
		if (tokens.isEmpty()) {
			throw new IllegalArgumentException("testCommand est vide");
		}
		String program = tokens.get(0);
		if (program.startsWith("/") || program.startsWith("../") || program.contains(":/")) {
			throw new IllegalArgumentException("les chemins absolus sont interdits");
		}
		if (!ALLOWED.contains(program)) {
			throw new IllegalArgumentException("programme non autorisé : " + program);
		}
		if (tokens.stream().anyMatch(FORBIDDEN::contains)) {
			throw new IllegalArgumentException("commande interdite dans testCommand");
		}
		return tokens;
	}
}
