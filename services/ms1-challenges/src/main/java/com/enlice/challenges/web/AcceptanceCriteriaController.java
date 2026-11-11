package com.enlice.challenges.web;

import com.enlice.challenges.dto.AcceptanceCriterionDto;
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
@RequestMapping("/api/challenges/{id}/acceptance-criteria")
@Tag(name = "Challenges", description = "Spec de tests d'acceptation (consommée par MS5)")
public class AcceptanceCriteriaController {

    private final ChallengeService challengeService;

    public AcceptanceCriteriaController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping
    @Operation(summary = "Lire la spec de tests d'acceptation")
    public List<AcceptanceCriterionDto> get(@PathVariable UUID id) {
        return challengeService.getCriteria(id);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ENTERPRISE','ADMIN')")
    @Operation(summary = "Définir la spec de tests d'acceptation (remplacement complet)")
    public List<AcceptanceCriterionDto> replace(@PathVariable UUID id,
                                                @Valid @RequestBody List<AcceptanceCriterionDto> criteria) {
        return challengeService.replaceCriteria(id, criteria);
    }
}
