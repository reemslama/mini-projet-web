package com.enlice.challenges.web;

import com.enlice.challenges.dto.PrizeDto;
import com.enlice.challenges.service.ChallengeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/challenges/{id}/prizes")
@Tag(name = "Challenges", description = "Primes du top 3 (lues par MS4)")
public class PrizeController {

    private final ChallengeService challengeService;

    public PrizeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping
    @Operation(summary = "Lire les primes (top 3)")
    public List<PrizeDto> get(@PathVariable UUID id) {
        return challengeService.getPrizes(id);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ENTERPRISE','ADMIN')")
    @Operation(summary = "Définir les primes (top 3, remplacement complet)")
    public List<PrizeDto> replace(@PathVariable UUID id, @Valid @RequestBody List<PrizeDto> prizes) {
        return challengeService.replacePrizes(id, prizes);
    }
}
