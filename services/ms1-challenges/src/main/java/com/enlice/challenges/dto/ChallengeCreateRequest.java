package com.enlice.challenges.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Corps de POST /api/challenges et PUT /api/challenges/{id}. */
public record ChallengeCreateRequest(
        @NotBlank(message = "est obligatoire")
        @Size(max = 140, message = "doit contenir au plus 140 caractères")
        String title,

        @NotBlank(message = "est obligatoire")
        String description,

        @NotNull(message = "est obligatoire")
        UUID enterpriseId,

        @NotNull(message = "est obligatoire")
        @Future(message = "doit être dans le futur")
        Instant deadline,

        Set<String> tags) {
}
