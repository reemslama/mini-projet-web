package com.enlice.participants.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record TeamCreate(
        @NotBlank(message = "est obligatoire")
        String name,

        @NotNull(message = "est obligatoire")
        UUID ownerId,

        Set<UUID> memberIds) {
}
