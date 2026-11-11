package com.enlice.participants.web;

import com.enlice.participants.domain.ApplicantType;
import com.enlice.participants.dto.ApplicationCreate;
import com.enlice.participants.dto.ApplicationResponse;
import com.enlice.participants.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/challenges/{challengeId}/applications")
@Tag(name = "Applications", description = "Candidatures aux défis")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "S'inscrire à un défi (déclenche application.registered)")
    public ResponseEntity<ApplicationResponse> apply(@PathVariable UUID challengeId,
                                                      @Valid @RequestBody ApplicationCreate request) {
        return ResponseEntity.ok(applicationService.apply(challengeId, request, Instant.now()));
    }

    @GetMapping
    @Operation(summary = "Lister les candidatures d'un défi")
    public ResponseEntity<java.util.List<ApplicationResponse>> applications(@PathVariable UUID challengeId) {
        return ResponseEntity.ok(applicationService.applicationsOf(challengeId));
    }
}
