package tn.arene.ms5.domain;

import java.util.regex.Pattern;

/** Redaction avant stockage et avant exposition GET /api/verifications/{id}/logs. */
public final class LogRedactor {

	private static final int MAX_BYTES = 1024 * 1024;
	private static final Pattern[] SECRETS = {
			Pattern.compile("ghp_[A-Za-z0-9]{20,}"),
			Pattern.compile("github_pat_[A-Za-z0-9_]{20,}"),
			Pattern.compile("AKIA[A-Z0-9]{16}"),
			Pattern.compile("(?i)(Bearer\\s+)[A-Za-z0-9._-]{12,}"),
			Pattern.compile("(?i)(https?://)[^/@\\s:]+:[^/@\\s]+@")
	};

	private LogRedactor() {
	}

	public static String redact(String logs) {
		if (logs == null || logs.isEmpty()) {
			return "";
		}
		String result = logs;
		for (Pattern pattern : SECRETS) {
			result = pattern.matcher(result).replaceAll("[REDACTED]");
		}
		if (result.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > MAX_BYTES) {
			result = result.substring(0, Math.min(result.length(), MAX_BYTES / 2))
					+ "\n[TRUNCATED BY MS5]\n";
		}
		return result;
	}
}
