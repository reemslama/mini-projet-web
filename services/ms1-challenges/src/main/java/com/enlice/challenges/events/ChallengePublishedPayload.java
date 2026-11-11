package com.enlice.challenges.events;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Payload de {@code challenge.published}. */
public record ChallengePublishedPayload(UUID challengeId, UUID enterpriseId, Instant deadline, Set<String> tags) {
}
