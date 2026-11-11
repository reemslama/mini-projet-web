package com.enlice.challenges.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/** Critère d'acceptation en entrée/sortie. */
public record AcceptanceCriterionDto(
        String id,

        @NotBlank(message = "est obligatoire")
        String description,

        @DecimalMin(value = "0.0", message = "doit être compris entre 0 et 1")
        @DecimalMax(value = "1.0", message = "doit être compris entre 0 et 1")
        BigDecimal weight,

        String testCommand) {
}
