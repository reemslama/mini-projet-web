package tn.arene.ms5.adapter.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import tn.arene.ms5.adapter.web.dto.AiSummaryResponse;
import tn.arene.ms5.adapter.web.dto.SimilarityReportResponse;
import tn.arene.ms5.adapter.web.dto.SimilarityRequest;
import tn.arene.ms5.adapter.web.dto.VerificationRequest;
import tn.arene.ms5.adapter.web.dto.VerificationResponse;
import tn.arene.ms5.application.usecase.AnalyzeSimilarity;
import tn.arene.ms5.application.usecase.LaunchVerification;
import tn.arene.ms5.application.usecase.QueryVerification;
import tn.arene.ms5.application.usecase.SummarizeWithAI;

/** Les 6 endpoints gelés de contracts/ms5-verification.openapi.yaml, sans aucun supplément. */
@RestController
public class VerificationRestController {

	private final LaunchVerification launch;
	private final QueryVerification query;
	private final AnalyzeSimilarity similarity;
	private final SummarizeWithAI summarize;

	public VerificationRestController(LaunchVerification launch, QueryVerification query,
			AnalyzeSimilarity similarity, SummarizeWithAI summarize) {
		this.launch = launch;
		this.query = query;
		this.similarity = similarity;
		this.summarize = summarize;
	}

	@Operation(summary = "Lancer une vérification (manuel ou rejeu)")
	@PostMapping(path = "/api/verifications", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<VerificationResponse> launch(
			@RequestBody VerificationRequest request,
			@RequestHeader(value = "X-Requested-By", required = false, defaultValue = "user:local") String actor) {
		var verification = launch.launch(new LaunchVerification.Request(
				request.submissionId(), request.challengeId(), request.repoUrl(), request.commitSha(),
				request.testCommand(), actor));
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(VerificationResponse.from(verification));
	}

	@Operation(summary = "État / résultat d'une vérification")
	@GetMapping(path = "/api/verifications/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public VerificationResponse get(@PathVariable UUID id) {
		return VerificationResponse.from(query.get(id));
	}

	@Operation(summary = "Logs d'exécution (texte brut, redactés)")
	@GetMapping(path = "/api/verifications/{id}/logs", produces = MediaType.TEXT_PLAIN_VALUE)
	public String logs(@PathVariable UUID id) {
		return query.logs(id);
	}

	@Operation(summary = "Analyser la similarité d'une soumission vs les autres (pgvector)")
	@PostMapping(path = "/api/similarity/analyze", produces = MediaType.APPLICATION_JSON_VALUE)
	public SimilarityReportResponse analyze(@RequestBody SimilarityRequest request) {
		return SimilarityReportResponse.from(similarity.analyze(
				new AnalyzeSimilarity.Request(request.submissionId(), request.challengeId())));
	}

	@Operation(summary = "Rapport de similarité déjà calculé")
	@GetMapping(path = "/api/similarity/{submissionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public SimilarityReportResponse getSimilarity(@PathVariable UUID submissionId) {
		return SimilarityReportResponse.from(similarity.latest(submissionId));
	}

	@Operation(summary = "Résumé IA d'une soumission pour le jury (amélioration, non bloquant)")
	@PostMapping(path = "/api/ai/summarize/{submissionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public AiSummaryResponse summarize(@PathVariable UUID submissionId) {
		SummarizeWithAI.Summary summary = summarize.summarize(submissionId);
		return new AiSummaryResponse(summary.submissionId(), summary.summary(),
				summary.model(), summary.generatedAt());
	}

}
