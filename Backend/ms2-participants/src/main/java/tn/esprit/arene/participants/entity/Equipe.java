package tn.esprit.arene.participants.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Équipe = un groupe d'utilisateurs qui participe ensemble à un défi.
 * Elle a un chef (Utilisateur) et des membres (MembreEquipe).
 */
@Entity
@Table(name = "equipes")
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String nom;

    private String description;

    /** Nombre maximum de membres (chef compris). */
    @Min(2)
    @Max(10)
    private int tailleMax = 5;

    /** Le chef d'équipe : un utilisateur (association N..1). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "chef_id")
    private Utilisateur chef;

    /** Composition de l'équipe (association 1..N vers la classe d'association MembreEquipe). */
    @OneToMany(mappedBy = "equipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MembreEquipe> membres = new ArrayList<>();

    private LocalDateTime dateCreation;

    @PrePersist
    void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }

    /** Ajoute un membre en gardant les deux côtés de l'association cohérents. */
    public void ajouterMembre(Utilisateur utilisateur, RoleEquipe role) {
        MembreEquipe membre = new MembreEquipe(this, utilisateur, role);
        membres.add(membre);
    }

    public boolean contient(UUID utilisateurId) {
        return membres.stream().anyMatch(m -> m.getUtilisateur().getId().equals(utilisateurId));
    }

    public boolean estComplete() {
        return membres.size() >= tailleMax;
    }

    // ---------- getters / setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getTailleMax() { return tailleMax; }
    public void setTailleMax(int tailleMax) { this.tailleMax = tailleMax; }
    public Utilisateur getChef() { return chef; }
    public void setChef(Utilisateur chef) { this.chef = chef; }
    public List<MembreEquipe> getMembres() { return membres; }
    public void setMembres(List<MembreEquipe> membres) { this.membres = membres; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
