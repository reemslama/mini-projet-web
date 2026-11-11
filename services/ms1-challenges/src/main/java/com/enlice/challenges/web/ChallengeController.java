package com.enlice.challenges.web;

import com.enlice.challenges.domain.ChallengeStatus;
import com.enlice.challenges.dto.ChallengeCreateRequest;
import com.enlice.challenges.dto.ChallengeResponse;
import com.enlice.challenges.service.ChallengeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/challenges")
@Tag(name = "Challenges", description = "Espace entreprise, défis et catalogue")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ENTERPRISE','ADMIN')")
    @Operation(summary = "Créer un défi (brouillon)")
    public ResponseEntity<ChallengeResponse> create(@Valid @RequestBody ChallengeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(challengeService.create(request));
    }

    @GetMapping
    @Operation(summary = "Catalogue / recherche de défis")
    public PageResponse<ChallengeResponse> list(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) ChallengeStatus status,
            @RequestParam(required = false) Double minPrize,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return challengeService.list(tag, status, minPrize, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un défi")
    public ChallengeResponse get(@PathVariable UUID id) {
        return challengeService.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENTERPRISE','ADMIN')")
    @Operation(summary = "Mettre à jour un défi (statut DRAFT uniquement)")
    public ChallengeResponse update(@PathVariable UUID id,
                                    @Valid @RequestBody ChallengeCreateRequest request) {
        return challengeService.update(id, request);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ENTERPRISE','ADMIN')")
    @Operation(summary = "Publier un défi (déclenche challenge.published)")
    public ChallengeResponse publish(@PathVariable UUID id) {
        return challengeService.publish(id);
    }
}
