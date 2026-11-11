package com.enlice.participants.dto;

import com.enlice.participants.domain.Developer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public record DeveloperResponse(
        UUID id,
        String keycloakId,
        String displayName,
        String email,
        Set<String> skills,
        Instant createdAt) {

    public static DeveloperResponse from(Developer developer) {
        return new DeveloperResponse(developer.getId(), developer.getKeycloakId(), developer.getDisplayName(),
                developer.getEmail(), new LinkedHashSet<>(developer.getSkills()), developer.getCreatedAt());
    }
}
