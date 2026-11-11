package com.enlice.participants.dto;

import com.enlice.participants.domain.ApplicantType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ApplicationCreate(
        @NotNull(message = "est obligatoire")
        ApplicantType applicantType,

        @NotNull(message = "est obligatoire")
        UUID applicantId) {
}
