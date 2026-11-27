package tn.esprit.arene.participants.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Format d'erreur commun renvoyé par toutes les routes. */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details) {
}
