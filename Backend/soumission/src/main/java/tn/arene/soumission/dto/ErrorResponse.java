package tn.arene.soumission.dto;

import java.time.Instant;

/** Enveloppe d'erreur transverse (§7.3 du document technique). */
public record ErrorResponse(
		Instant timestamp,
		int status,
		String error,
		String message,
		String path) {
}
