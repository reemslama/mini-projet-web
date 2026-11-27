package tn.esprit.arene.participants.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.arene.participants.entity.Candidature;
import tn.esprit.arene.participants.entity.Equipe;
import tn.esprit.arene.participants.entity.Utilisateur;
import tn.esprit.arene.participants.service.UtilisateurService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Utilisateurs", description = "Profils des développeurs (personnes)")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @PostMapping
    @Operation(summary = "Créer un profil développeur")
    public ResponseEntity<Utilisateur> creer(@Valid @RequestBody Utilisateur u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.creer(u));
    }

    @GetMapping
    @Operation(summary = "Lister les utilisateurs")
    public List<Utilisateur> lister() {
        return utilisateurService.lister();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un utilisateur")
    public Utilisateur trouver(@PathVariable UUID id) {
        return utilisateurService.trouver(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un utilisateur")
    public Utilisateur modifier(@PathVariable UUID id, @Valid @RequestBody Utilisateur u) {
        return utilisateurService.modifier(id, u);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur")
    public ResponseEntity<Void> supprimer(@PathVariable UUID id) {
        utilisateurService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/teams")
    @Operation(summary = "Équipes dont l'utilisateur est membre")
    public List<Equipe> equipes(@PathVariable UUID id) {
        return utilisateurService.equipesDe(id);
    }


}
