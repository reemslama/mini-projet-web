package tn.arene.soumission.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import tn.arene.soumission.dto.SubmissionCreateRequest;
import tn.arene.soumission.dto.SubmissionResponse;
import tn.arene.soumission.dto.VerdictRequest;
import tn.arene.soumission.dto.VerdictResponse;
import tn.arene.soumission.entities.SubmissionStatus;
import tn.arene.soumission.services.ISubmissionService;
import tn.arene.soumission.services.IVerdictService;

/** Les 6 endpoints MS3 du contrat gelé (préfixe Gateway /ms3/api/submissions). */
@Tag(name = "Submissions", description = "Cycle de vie des soumissions MS3 (contrat gelé).")
@RestController
@RequestMapping("/api/submissions")
public class SubmissionRestController {

	private final ISubmissionService submissionService;
	private final IVerdictService verdictService;

	public SubmissionRestController(ISubmissionService submissionService, IVerdictService verdictService) {
		this.submissionService = submissionService;
		this.verdictService = verdictService;
	}

	@Operation(summary = "Créer une soumission (brouillon)",
			description = "Crée une soumission rattachée à un défi et à un candidat (MS1/MS2).")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Soumission créée"),
			@ApiResponse(responseCode = "400", description = "Requête invalide"),
			@ApiResponse(responseCode = "404", description = "Défi ou candidat introuvable")
	})
	@SecurityRequirement(name = "bearerAuth")
	@PostMapping
	public ResponseEntity<SubmissionResponse> create(@RequestBody SubmissionCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(submissionService.create(request));
	}

	@Operation(summary = "Lister les soumissions d'un défi",
			description = "Filtre optionnel par statut ; référence MS1 par challengeId (UUID).")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "Liste des soumissions"))
	@GetMapping
	public List<SubmissionResponse> list(
			@Parameter(description = "UUID du défi (MS1)", required = true) @RequestParam UUID challengeId,
			@Parameter(description = "Filtre de statut") @RequestParam(required = false) SubmissionStatus status) {
		return submissionService.findByChallengeId(challengeId, status);
	}

	@Operation(summary = "Détail d'une soumission")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Soumission trouvée"),
			@ApiResponse(responseCode = "404", description = "Soumission introuvable")
	})
	@GetMapping("/{id}")
	public SubmissionResponse getById(
			@Parameter(description = "UUID de la soumission", required = true) @PathVariable UUID id) {
		return submissionService.findById(id);
	}

	/**
	 * TODO intégration : commitSha proviendra du dépôt et deadline de MS1 (OpenFeign, hors périmètre).
	 * Les paramètres de requête sont facultatifs pour rester conforme au contrat gelé.
	 */
	@Operation(summary = "Soumettre définitivement",
			description = "Fige le commit, contrôle la deadline et émet submission.submitted vers MS5.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Soumission figée"),
			@ApiResponse(responseCode = "409", description = "Date limite dépassée ou soumission déjà figée")
	})
	@SecurityRequirement(name = "bearerAuth")
	@PostMapping("/{id}/submit")
	public SubmissionResponse submit(
			@Parameter(description = "UUID de la soumission", required = true) @PathVariable UUID id,
			@Parameter(description = "Commit à figer (facultatif, conforme au contrat)") @RequestParam(required = false) String commitSha,
			@Parameter(description = "Date limite (facultative, conforme au contrat)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadline) {
		return submissionService.submit(id, commitSha, deadline);
	}

	@Operation(summary = "Lire le verdict de vérification")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Verdict disponible"),
			@ApiResponse(responseCode = "404", description = "Pas encore de verdict")
	})
	@GetMapping("/{id}/verdict")
	public VerdictResponse getVerdict(
			@Parameter(description = "UUID de la soumission", required = true) @PathVariable UUID id) {
		return verdictService.findBySubmissionId(id);
	}

	@Operation(summary = "Enregistrer le verdict (appel interne de MS5)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Verdict enregistré"),
			@ApiResponse(responseCode = "403", description = "Rôle non autorisé"),
			@ApiResponse(responseCode = "404", description = "Soumission introuvable")
	})
	@SecurityRequirement(name = "bearerAuth")
	@PutMapping("/{id}/verdict")
	public VerdictResponse putVerdict(
			@Parameter(description = "UUID de la soumission", required = true) @PathVariable UUID id,
			@RequestBody VerdictRequest request) {
		return verdictService.deposit(id, request);
	}
}
