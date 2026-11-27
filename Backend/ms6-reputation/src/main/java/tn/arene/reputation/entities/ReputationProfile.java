package tn.arene.reputation.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Agregat : un profil de reputation par developpeur.
 * developerId reference MS2 par identifiant uniquement (pas de cle etrangere entre services).
 *
 * Associations :
 *  - OneToMany -> Badge           (composition : le profil detient ses badges)
 *  - OneToMany -> ReputationEvent (historique des evenements agreges)
 */
@Entity
@Table(name = "reputation_profile")
@Getter
@Setter
@NoArgsConstructor
public class ReputationProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID developerId;

    /** Replique depuis MS2 (reference), utilise par le classement. */
    @Column(nullable = false)
    private String displayName;

    private int score;
    private int challengesWon;
    private int submissionsPassed;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_skill", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("awardedAt ASC")
    private List<Badge> badges = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("occurredAt DESC")
    private List<ReputationEvent> events = new ArrayList<>();

    public static ReputationProfile of(UUID developerId, String displayName) {
        ReputationProfile p = new ReputationProfile();
        p.developerId = developerId;
        p.displayName = displayName;
        return p;
    }

    /** Applique un evenement : met a jour score et compteurs. */
    public void apply(ReputationEvent e) {
        events.add(e);
        e.setProfile(this);
        score += e.getWeight();
        switch (e.getType()) {
            case CHALLENGE_WON -> challengesWon++;
            case SUBMISSION_PASSED -> submissionsPassed++;
            case SUBMISSION_REJECTED -> { /* aucun point */ }
        }
        updatedAt = LocalDateTime.now();
    }

    /** Recalcule score et compteurs a partir de l'historique (apres suppression d'un evenement). */
    public void recompute() {
        score = events.stream().mapToInt(ReputationEvent::getWeight).sum();
        challengesWon = (int) events.stream().filter(e -> e.getType() == ReputationEventType.CHALLENGE_WON).count();
        submissionsPassed = (int) events.stream().filter(e -> e.getType() == ReputationEventType.SUBMISSION_PASSED).count();
        updatedAt = LocalDateTime.now();
    }

    public boolean hasBadge(String code) {
        return badges.stream().anyMatch(b -> b.matches(code));
    }

    public void addBadge(Badge b) {
        if (hasBadge(b.getCode())) return;
        badges.add(b);
        b.setProfile(this);
        updatedAt = LocalDateTime.now();
    }
}
