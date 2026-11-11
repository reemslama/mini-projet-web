package com.enlice.participants.web;

import com.enlice.participants.dto.TeamCreate;
import com.enlice.participants.dto.TeamResponse;
import com.enlice.participants.service.TeamService;
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
@Tag(name = "Teams", description = "Équipes de développeurs")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping("/teams")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Créer une équipe")
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamCreate request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.create(request));
    }

    @GetMapping("/teams/{id}")
    @Operation(summary = "Détail d'une équipe")
    public TeamResponse get(@PathVariable UUID id) {
        return teamService.get(id);
    }
}
