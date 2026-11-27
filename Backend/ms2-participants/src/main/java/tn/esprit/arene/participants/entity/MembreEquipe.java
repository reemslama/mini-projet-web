package tn.esprit.arene.participants.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Classe d'association entre Utilisateur et Equipe (relation N..N porteuse d'attributs) :
 * un utilisateur peut être dans plusieurs équipes, une équipe a plusieurs utilisateurs,
 * et on veut savoir QUEL rôle il y joue et DEPUIS QUAND.
 */
@Entity
@Table(name = "membres_equipe",
       uniqueConstraints = @UniqueConstraint(columnNames = {"equipe_id", "utilisateur_id"}))
public class MembreEquipe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore // évite la boucle JSON Equipe -> membres -> equipe -> ...
    @ManyToOne(optional = false)
    @JoinColumn(name = "equipe_id")
    private Equipe equipe;

    @ManyToOne(optional = false)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    private RoleEquipe role = RoleEquipe.MEMBRE;

    private LocalDateTime dateAdhesion;

    @PrePersist
    void onCreate() {
        this.dateAdhesion = LocalDateTime.now();
    }

    public MembreEquipe() {
    }

    public MembreEquipe(Equipe equipe, Utilisateur utilisateur, RoleEquipe role) {
        this.equipe = equipe;
        this.utilisateur = utilisateur;
        this.role = role;
    }

    // ---------- getters / setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Equipe getEquipe() { return equipe; }
    public void setEquipe(Equipe equipe) { this.equipe = equipe; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
    public RoleEquipe getRole() { return role; }
    public void setRole(RoleEquipe role) { this.role = role; }
    public LocalDateTime getDateAdhesion() { return dateAdhesion; }
    public void setDateAdhesion(LocalDateTime dateAdhesion) { this.dateAdhesion = dateAdhesion; }
}
