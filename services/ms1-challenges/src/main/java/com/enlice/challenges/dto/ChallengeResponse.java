package com.enlice.challenges.dto;

import com.enlice.challenges.domain.Challenge;
import com.enlice.challenges.domain.ChallengeStatus;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Représentation d'un défi conforme au schéma OpenAPI {@code Challenge}. */
public record ChallengeResponse(
        UUID id,
        String title,
        String description,
        UUID enterpriseId,
        Instant deadline,
        Set<String> tags,
        ChallengeStatus status,
        Instant createdAt,
        Instant publishedAt) {

    public static ChallengeResponse from(Challenge challenge) {
        return new ChallengeResponse(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getEnterpriseId(),
                challenge.getDeadline(),
                new LinkedHashSet<>(challenge.getTags()),
                challenge.getStatus(),
                challenge.getCreatedAt(),
                challenge.getPublishedAt());
    }
}
