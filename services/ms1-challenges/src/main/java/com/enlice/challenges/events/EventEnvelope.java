package com.enlice.challenges.events;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

/**
 * Enveloppe commune des événements métier En Lice.
 * Le payload est un {@link JsonNode} : la désérialisation est robuste et chaque consommateur
 * le convertit vers son propre type via {@code ObjectMapper.convertValue}.
 */
public record EventEnvelope(UUID eventId, String type, Instant occurredAt, String producer, int version,
                            JsonNode payload) {
}