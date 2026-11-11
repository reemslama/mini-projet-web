package com.enlice.submissions.web;

import com.enlice.submissions.domain.SubmissionStatus;
import com.enlice.submissions.domain.Verdict;
import com.enlice.submissions.dto.SubmissionCreate;
import com.enlice.submissions.dto.SubmissionResponse;
import com.enlice.submissions.dto.VerdictResponse;
import com.enlice.submissions.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/submissions")
@Tag(name = "Submissions", description = "Dépôts GitHub, cycles de vie, verdicts")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Créer une soumission (brouillon)")
    public ResponseEntity<SubmissionResponse> create(@Valid @RequestBody SubmissionCreate request,
                                                      Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(submissionService.create(UUID.fromString(authentication.getName()), request, Instant.now()));
    }

    @GetMapping
    @Operation(summary = "Lister les soumissions d'un défi")
    public List<SubmissionResponse> list(
            @RequestParam UUID challengeId,
            @RequestParam(required = false) SubmissionStatus status) {
        return submissionService.submissionsOf(challengeId, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une soumission")
    public SubmissionResponse get(@PathVariable UUID id) {
        return submissionService.get(id);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Soumettre définitivement (fige le commit, vérifie la date limite, émet submission.submitted)")
    public ResponseEntity<SubmissionResponse> submit(@PathVariable UUID id) {
        return ResponseEntity.ok(submissionService.submit(id, Instant.now()));
    }

    @GetMapping("/{id}/verdict")
    @Operation(summary = "Lire le verdict de vérification")
    public VerdictResponse verdict(@PathVariable UUID id) {
        return submissionService.verdict(id);
    }

    @PutMapping("/{id}/verdict")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Enregistrer le verdict (appel interne de MS5)")
    public VerdictResponse recordVerdict(@PathVariable UUID id, @RequestBody VerdictRequest body) {
        Verdict verdict = new Verdict(id, body.result(), body.passed(), body.failed(), body.total(),
                body.coverage(), body.logsUrl(), body.verifiedAt() == null ? Instant.now() : body.verifiedAt());
        return submissionService.recordVerdict(id, verdict);
    }

    public record VerdictRequest(String result, Integer passed, Integer failed, Integer total, Double coverage,
                                 String logsUrl, Instant verifiedAt) {
    }
}
