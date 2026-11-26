package tn.arene.ms5.adapter.web.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Contrat : seul submissionId est obligatoire ; les autres champs sont enrichis côté serveur. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VerificationRequest(UUID submissionId, UUID challengeId, String repoUrl,
		String commitSha, String testCommand) {
}
