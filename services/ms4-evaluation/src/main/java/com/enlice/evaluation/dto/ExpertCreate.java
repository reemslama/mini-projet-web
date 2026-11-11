package com.enlice.evaluation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record ExpertCreate(
        @NotBlank(message = "est obligatoire")
        @Size(max = 64, message = "doit contenir au plus 64 caractères")
        String keycloakId,

        @NotBlank(message = "est obligatoire")
        @Size(max = 120, message = "doit contenir au plus 120 caractères")
        String displayName,

        Set<String> domains) {
}
