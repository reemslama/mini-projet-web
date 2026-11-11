package com.enlice.participants.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Participants", description = "Développeurs, équipes, candidatures aux défis")
public class DeveloperController {

    private final com.enlice.participants.service.DeveloperService developerService;

    public DeveloperController(com.enlice.participants.service.DeveloperService developerService) {
        this.developerService = developerService;
    }

    @PostMapping("/developers")
    @Operation(summary = "Créer un profil développeur (idempotent sur keycloakId)")
    public ResponseEntity<com.enlice.participants.dto.DeveloperResponse> create(
            @Valid @RequestBody com.enlice.participants.dto.DeveloperCreate request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(developerService.create(request));
    }

    @GetMapping("/developers/{id}")
    @Operation(summary = "Profil développeur")
    public com.enlice.participants.dto.DeveloperResponse get(@PathVariable UUID id) {
        return developerService.get(id);
    }

    @GetMapping("/developers/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Profil développeur courant (dérivation du JWT)")
    public com.enlice.participants.dto.DeveloperResponse me(jakarta.security.auth. Authentication authentication) {
        return developerService.getByKeycloakId(authentication.getName());
    }
}
