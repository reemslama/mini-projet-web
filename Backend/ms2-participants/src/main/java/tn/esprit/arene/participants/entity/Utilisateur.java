package tn.esprit.arene.participants.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Utilisateur = une PERSONNE réelle qui participe aux défis (rôle Keycloak DEVELOPER).
 * Il peut participer seul, ou appartenir à une ou plusieurs équipes (via MembreEquipe).
 */
@Entity
@Table(name = "utilisateurs") // "user" est un mot réservé en SQL
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 50)
    private String prenom;

    @NotBlank
    @Size(max = 50)
    private String nom;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    /** Pseudo public affiché dans les classements. */
    @NotBlank
    @Column(unique = true, nullable = false)
    private String pseudo;

    /** Compte GitHub : utilisé par MS3 pour les dépôts soumis. */
    private String githubUsername;

    private String telephone;
    private String pays;
    private String ville;

    @Size(max = 500)
    private String bio;

    @Enumerated(EnumType.STRING)
    private Niveau niveau = Niveau.JUNIOR;

    /** Compétences techniques : Java, Spring, Angular, Docker... */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "utilisateur_competences", joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "competence")
    private Set<String> competences = new LinkedHashSet<>();

    /** Disponible pour rejoindre une équipe ? */
    private boolean disponible = true;

    @Enumerated(EnumType.STRING)
    private StatutCompte statut = StatutCompte.ACTIF;

    private LocalDateTime dateInscription;

    @PrePersist
    void onCreate() {
        this.dateInscription = LocalDateTime.now();
    }

    public Utilisateur() {
    }

    public Utilisateur(String prenom, String nom, String email, String pseudo, String githubUsername,
                       String pays, String ville, Niveau niveau, Collection<String> competences) {
        this.prenom = prenom;
        this.nom = nom;
        this.email = email;
        this.pseudo = pseudo;
        this.githubUsername = githubUsername;
        this.pays = pays;
        this.ville = ville;
        this.niveau = niveau;
        this.competences = new LinkedHashSet<>(competences);
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    // ---------- getters / setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPseudo() { return pseudo; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }
    public String getGithubUsername() { return githubUsername; }
    public void setGithubUsername(String githubUsername) { this.githubUsername = githubUsername; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public Niveau getNiveau() { return niveau; }
    public void setNiveau(Niveau niveau) { this.niveau = niveau; }
    public Set<String> getCompetences() { return competences; }
    public void setCompetences(Set<String> competences) { this.competences = competences; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public StatutCompte getStatut() { return statut; }
    public void setStatut(StatutCompte statut) { this.statut = statut; }
    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }
}
