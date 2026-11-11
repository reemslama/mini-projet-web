package com.enlice.submissions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record SubmissionCreate(
        @NotNull(message = "est obligatoire")
        UUID challengeId,

        @NotNull(message = "est obligatoire")
        UUID applicantId,

        @NotBlank(message = "est obligatoire")
        @Pattern(regexp = "^https://github\\.com/.+", message = "doit être une URL GitHub publique")
        String repoUrl,

        @NotBlank(message = "est obligatoire")
        String branch) {
}
