package tn.arene.ms5.adapter.web.dto;

import java.time.LocalDateTime;

/** Enveloppe d'erreur transverse (§ 7.3 du document technique). */
public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
}
