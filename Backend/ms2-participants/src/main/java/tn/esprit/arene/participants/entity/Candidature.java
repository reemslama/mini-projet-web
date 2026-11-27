package tn.esprit.arene.participants.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Candidature = inscription à un défi.
 * - SOLO   : portée par un Utilisateur (equipe = null)
 * - EQUIPE : portée par une Equipe, déposée par un utilisateur membre (le "deposant")
 *
 * challengeId référence un défi de MS1 par IDENTIFIANT uniquement :
 * pas de clé étrangère entre microservices (une base par service).
 */
@Entity
@Table(name = "candidatures")
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Identifiant du défi géré par MS1 · Challenges. */
    @NotNull
    @Column(nullable = false)
    private UUID challengeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCandidature type;

    /** La personne qui dépose la candidature (toujours renseignée). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "deposant_id")
    private Utilisateur deposant;

    /** L'équipe concernée si type = EQUIPE, sinon null. */
    @ManyToOne
    @JoinColumn(name = "equipe_id")
    private Equipe equipe;

    @Enumerated(EnumType.STRING)
    private StatutCandidature statut = StatutCandidature.EN_ATTENTE;

    /** Pourquoi je participe / présentation du projet. */
    @Column(length = 1000)
    private String motivation;

    private LocalDateTime dateCandidature;
    private LocalDateTime dateMiseAJour;

    @PrePersist
    void onCreate() {
        this.dateCandidature = LocalDateTime.now();
        this.dateMiseAJour = this.dateCandidature;
    }

    @PreUpdate
    void onUpdate() {
        this.dateMiseAJour = LocalDateTime.now();
    }

    // ---------- getters / setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getChallengeId() { return challengeId; }
    public void setChallengeId(UUID challengeId) { this.challengeId = challengeId; }
    public TypeCandidature getType() { return type; }
    public void setType(TypeCandidature type) { this.type = type; }
    public Utilisateur getDeposant() { return deposant; }
    public void setDeposant(Utilisateur deposant) { this.deposant = deposant; }
    public Equipe getEquipe() { return equipe; }
    public void setEquipe(Equipe equipe) { this.equipe = equipe; }
    public StatutCandidature getStatut() { return statut; }
    public void setStatut(StatutCandidature statut) { this.statut = statut; }
    public String getMotivation() { return motivation; }
    public void setMotivation(String motivation) { this.motivation = motivation; }
    public LocalDateTime getDateCandidature() { return dateCandidature; }
    public void setDateCandidature(LocalDateTime dateCandidature) { this.dateCandidature = dateCandidature; }
    public LocalDateTime getDateMiseAJour() { return dateMiseAJour; }
    public void setDateMiseAJour(LocalDateTime dateMiseAJour) { this.dateMiseAJour = dateMiseAJour; }
}
