package tn.arene.reputation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.arene.reputation.dto.BadgeRequest;
import tn.arene.reputation.dto.ProfileRequest;
import tn.arene.reputation.dto.ReputationEventRequest;
import tn.arene.reputation.dto.Responses.*;
import tn.arene.reputation.services.IReputationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoints du contrat gele (contracts/ms6-reputation.openapi.yaml)
 * + CRUD complet des profils sous /api/reputation/profiles.
 */
@RestController
@RequiredArgsConstructor
public class ReputationRestController {

    private final IReputationService service;

    @Value("${server.port}")
    private String port;

    // ---------------------------------------------------- contrat : lecture

    @Tag(name = "Reputation")
    @Operation(summary = "Profil de reputation d'un developpeur")
    @GetMapping("/api/developers/{id}/reputation")
    public ProfileResponse reputation(@PathVariable UUID id) {
        return service.findByDeveloper(id);
    }

    @Tag(name = "Reputation")
    @Operation(summary = "Badges obtenus")
    @GetMapping("/api/developers/{id}/badges")
    public List<BadgeResponse> badges(@PathVariable UUID id) {
        return service.findBadges(id);
    }

    @Tag(name = "Rankings")
    @Operation(summary = "Classement global, filtrable par competence (calcule, non stocke)")
    @GetMapping("/api/rankings")
    public List<RankingEntry> rankings(@RequestParam(required = false) String skill,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return service.ranking(skill, page, size);
    }

    // --------------------------------------------------- contrat : ingestion

    @Tag(name = "Internal")
    @Operation(summary = "Ingestion d'evenements (winners.declared, verification.completed). Doublon = ignore")
    @PostMapping("/api/reputation/events")
    public ResponseEntity<Map<String, String>> ingest(@Valid @RequestBody ReputationEventRequest req) {
        boolean applied = service.ingest(req);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("status", applied ? "APPLIED" : "DUPLICATE_IGNORED"));
    }

    // -------------------------------------------------------- CRUD profils

    @Tag(name = "Profiles CRUD")
    @GetMapping("/api/reputation/profiles")
    public List<ProfileResponse> allProfiles() {
        return service.findAllProfiles();
    }

    @Tag(name = "Profiles CRUD")
    @GetMapping("/api/reputation/profiles/{id}")
    public ProfileResponse profile(@PathVariable Long id) {
        return service.findProfile(id);
    }

    @Tag(name = "Profiles CRUD")
    @PostMapping("/api/reputation/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse create(@Valid @RequestBody ProfileRequest req) {
        return service.createProfile(req);
    }

    @Tag(name = "Profiles CRUD")
    @PutMapping("/api/reputation/profiles/{id}")
    public ProfileResponse update(@PathVariable Long id, @Valid @RequestBody ProfileRequest req) {
        return service.updateProfile(id, req);
    }

    @Tag(name = "Profiles CRUD")
    @DeleteMapping("/api/reputation/profiles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteProfile(id);
    }

    // ------------------------------------------- associations : badges / events

    @Tag(name = "Badges")
    @Operation(summary = "Attribuer un badge manuellement")
    @PostMapping("/api/developers/{id}/badges")
    @ResponseStatus(HttpStatus.CREATED)
    public BadgeResponse award(@PathVariable UUID id, @Valid @RequestBody BadgeRequest req) {
        return service.awardBadge(id, req);
    }

    @Tag(name = "Badges")
    @DeleteMapping("/api/reputation/badges/{badgeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@PathVariable Long badgeId) {
        service.revokeBadge(badgeId);
    }

    @Tag(name = "Events")
    @Operation(summary = "Historique des evenements d'un developpeur")
    @GetMapping("/api/developers/{id}/events")
    public List<EventResponse> events(@PathVariable UUID id) {
        return service.findEvents(id);
    }

    @Tag(name = "Events")
    @Operation(summary = "Supprimer un evenement (le score est recalcule)")
    @DeleteMapping("/api/reputation/events/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long eventId) {
        service.deleteEvent(eventId);
    }

    // ------------------------------------------------------------- instance

    @Tag(name = "Instance")
    @Operation(summary = "Port de l'instance qui repond (utile avec 2 instances)")
    @GetMapping("/api/reputation/instance")
    public Map<String, String> instance() {
        return Map.of("service", "ms6-reputation", "port", port);
    }
}
