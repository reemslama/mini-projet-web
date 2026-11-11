package com.enlice.challenges.web;

import java.time.Instant;

/**
 * Enveloppe d'erreur unique de la plateforme En Lice.
 */
public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path);
    }
}