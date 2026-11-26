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

import tn.arene.soumission.dto.SubmissionCreateRequest;
import tn.arene.soumission.dto.SubmissionResponse;
import tn.arene.soumission.dto.VerdictRequest;
import tn.arene.soumission.dto.VerdictResponse;
import tn.arene.soumission.entities.SubmissionStatus;
import tn.arene.soumission.services.ISubmissionService;
import tn.arene.soumission.services.IVerdictService;

/** Les 6 endpoints MS3 du contrat gelé (préfixe Gateway /ms3/api/submissions). */
@RestController
@RequestMapping("/api/submissions")
public class SubmissionRestController {

	private final ISubmissionService submissionService;
	private final IVerdictService verdictService;

	public SubmissionRestController(ISubmissionService submissionService, IVerdictService verdictService) {
		this.submissionService = submissionService;
		this.verdictService = verdictService;
	}

	@PostMapping
	public ResponseEntity<SubmissionResponse> create(@RequestBody SubmissionCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(submissionService.create(request));
	}

	@GetMapping
	public List<SubmissionResponse> list(
			@RequestParam UUID challengeId,
			@RequestParam(required = false) SubmissionStatus status) {
		return submissionService.findByChallengeId(challengeId, status);
	}

	@GetMapping("/{id}")
	public SubmissionResponse getById(@PathVariable UUID id) {
		return submissionService.findById(id);
	}

	/**
	 * TODO intégration : commitSha proviendra du dépôt et deadline de MS1 (OpenFeign, hors périmètre).
	 * Les paramètres de requête sont facultatifs pour rester conforme au contrat gelé.
	 */
	@PostMapping("/{id}/submit")
	public SubmissionResponse submit(
			@PathVariable UUID id,
			@RequestParam(required = false) String commitSha,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadline) {
		return submissionService.submit(id, commitSha, deadline);
	}

	@GetMapping("/{id}/verdict")
	public VerdictResponse getVerdict(@PathVariable UUID id) {
		return verdictService.findBySubmissionId(id);
	}

	@PutMapping("/{id}/verdict")
	public VerdictResponse putVerdict(@PathVariable UUID id, @RequestBody VerdictRequest request) {
		return verdictService.deposit(id, request);
	}
}
