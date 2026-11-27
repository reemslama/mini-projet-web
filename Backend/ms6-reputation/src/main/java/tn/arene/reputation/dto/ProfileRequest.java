package tn.arene.reputation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/** Creation / modification d'un profil. */
public record ProfileRequest(
        @NotNull UUID developerId,
        @NotBlank String displayName,
        List<String> skills) {
}
