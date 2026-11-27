package tn.arene.reputation.dto;

import jakarta.validation.constraints.NotBlank;

/** Attribution manuelle d'un badge. */
public record BadgeRequest(@NotBlank String code, @NotBlank String label) {
}
