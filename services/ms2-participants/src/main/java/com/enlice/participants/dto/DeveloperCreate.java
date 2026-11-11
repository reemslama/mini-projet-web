package com.enlice.participants.dto;

import com.enlice.participants.domain.Developer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public record DeveloperCreate(
        @NotBlank(message = "est obligatoire")
        @Size(max = 64, message = "doit contenir au plus 64 caractères")
        String keycloakId,

        @NotBlank(message = "est obligatoire")
        @Size(max = 120, message = "doit contenir au plus 120 caractères")
        String displayName,

        @Email(message = "doit être une adresse email valide")
        String email,

        Set<String> skills) {

    public DeveloperResponse toResponse(Developer developer) {
        return new DeveloperResponse(developer.getId(), developer.getKeycloakId(), developer.getDisplayName(),
                developer.getEmail(), new LinkedHashSet<>(developer.getSkills()), developer.getCreatedAt());
    }
}
