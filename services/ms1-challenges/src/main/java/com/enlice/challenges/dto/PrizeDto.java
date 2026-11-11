package com.enlice.challenges.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Prime en entrée/sortie (schéma OpenAPI {@code Prize}). */
public record PrizeDto(
        @NotNull(message = "est obligatoire")
        @Min(value = 1, message = "doit être compris entre 1 et 3")
        @Max(value = 3, message = "doit être compris entre 1 et 3")
        Integer rank,

        @NotNull(message = "est obligatoire")
        @DecimalMin(value = "0.0", message = "doit être positif ou nul")
        BigDecimal amount,

        String currency) {
}
