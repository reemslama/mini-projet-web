package tn.arene.ms1challenges.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.arene.ms1challenges.dto.*;
import tn.arene.ms1challenges.entities.ChallengeStatus;
import tn.arene.ms1challenges.services.IChallengeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
@Tag(name = "Challenges", description = "Endpoints de gestion des défis, critères et primes")
public class ChallengeRestController {

    private final IChallengeService challengeService;

    @Operation(summary = "Créer un défi (brouillon)", description = "Crée un nouveau défi avec statut DRAFT")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Défi créé",
                    content = @Content(schema = @Schema(implementation = ChallengeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ChallengeResponse> createChallenge(@Valid @RequestBody ChallengeCreateRequest request) {
        ChallengeResponse response = challengeService.createChallenge(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Catalogue / recherche de défis", description = "Recherche paginée avec filtres optionnels")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page de défis trouvés")
    })
    @GetMapping
    public ResponseEntity<PageResponse<ChallengeResponse>> searchChallenges(
            @Parameter(description = "Tag de filtrage") @RequestParam(required = false) String tag,
            @Parameter(description = "Statut du défi") @RequestParam(required = false) ChallengeStatus status,
            @Parameter(description = "Montant minimum de prime") @RequestParam(required = false) Double minPrize,
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de page") @RequestParam(defaultValue = "20") int size
    ) {
        Page<ChallengeResponse> resultPage = challengeService.searchChallenges(tag, status, minPrize, page, size);
        return ResponseEntity.ok(PageResponse.fromPage(resultPage));
    }

    @Operation(summary = "Détail d'un défi", description = "Récupère les informations d'un défi par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Détail du défi",
                    content = @Content(schema = @Schema(implementation = ChallengeResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ressource introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ChallengeResponse> getChallengeById(@PathVariable UUID id) {
        ChallengeResponse response = challengeService.getChallengeById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mettre à jour un défi (statut DRAFT)", description = "Met à jour les informations du défi")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Défi mis à jour",
                    content = @Content(schema = @Schema(implementation = ChallengeResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ressource introuvable"),
            @ApiResponse(responseCode = "409", description = "Conflit : le défi n'est plus en statut DRAFT")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ChallengeResponse> updateChallenge(
            @PathVariable UUID id,
            @Valid @RequestBody ChallengeCreateRequest request
    ) {
        ChallengeResponse response = challengeService.updateChallenge(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Publier un défi", description = "Passe le défi du statut DRAFT au statut PUBLISHED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Défi publié",
                    content = @Content(schema = @Schema(implementation = ChallengeResponse.class))),
            @ApiResponse(responseCode = "409", description = "Défi déjà publié ou incomplet")
    })
    @PostMapping("/{id}/publish")
    public ResponseEntity<ChallengeResponse> publishChallenge(@PathVariable UUID id) {
        ChallengeResponse response = challengeService.publishChallenge(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lire la spec de tests d'acceptation", description = "Consommé notamment par MS5 pour exécuter les tests dans le sandbox")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des critères d'acceptation")
    })
    @GetMapping("/{id}/acceptance-criteria")
    public ResponseEntity<List<AcceptanceCriterionDto>> getAcceptanceCriteria(@PathVariable UUID id) {
        List<AcceptanceCriterionDto> criteria = challengeService.getAcceptanceCriteria(id);
        return ResponseEntity.ok(criteria);
    }

    @Operation(summary = "Définir la spec de tests d'acceptation", description = "Met à jour l'ensemble des critères d'acceptation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Critères mis à jour")
    })
    @PutMapping("/{id}/acceptance-criteria")
    public ResponseEntity<List<AcceptanceCriterionDto>> updateAcceptanceCriteria(
            @PathVariable UUID id,
            @Valid @RequestBody List<AcceptanceCriterionDto> criteria
    ) {
        List<AcceptanceCriterionDto> updated = challengeService.updateAcceptanceCriteria(id, criteria);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Lire les primes (top 3)", description = "Récupère les dotations du Top 3 pour ce défi")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des primes")
    })
    @GetMapping("/{id}/prizes")
    public ResponseEntity<List<PrizeDto>> getPrizes(@PathVariable UUID id) {
        List<PrizeDto> prizes = challengeService.getPrizes(id);
        return ResponseEntity.ok(prizes);
    }

    @Operation(summary = "Définir les primes (top 3)", description = "Définit les primes du rang 1 au rang 3")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Primes mises à jour")
    })
    @PutMapping("/{id}/prizes")
    public ResponseEntity<List<PrizeDto>> updatePrizes(
            @PathVariable UUID id,
            @Valid @RequestBody List<PrizeDto> prizes
    ) {
        List<PrizeDto> updated = challengeService.updatePrizes(id, prizes);
        return ResponseEntity.ok(updated);
    }
}
