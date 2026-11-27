package tn.arene.ms1challenges.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "challenges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String description;

    @Column(name = "enterprise_id", nullable = false)
    private UUID enterpriseId;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "challenge_tags", joinColumns = @JoinColumn(name = "challenge_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ChallengeStatus status = ChallengeStatus.DRAFT;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "challenge_id")
    @Builder.Default
    private List<AcceptanceCriterion> acceptanceCriteria = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "challenge_prizes", joinColumns = @JoinColumn(name = "challenge_id"))
    @Builder.Default
    private List<Prize> prizes = new ArrayList<>();

    public boolean isOpen() {
        return (status == ChallengeStatus.PUBLISHED || status == ChallengeStatus.IN_PROGRESS)
                && (deadline == null || LocalDateTime.now().isBefore(deadline));
    }

    public boolean acceptsApplications() {
        return status == ChallengeStatus.PUBLISHED && (deadline == null || LocalDateTime.now().isBefore(deadline));
    }

    public void publish() {
        if (this.status != ChallengeStatus.DRAFT) {
            throw new IllegalStateException("Seul un défi en statut DRAFT peut être publié.");
        }
        this.status = ChallengeStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void update(String title, String description, LocalDateTime deadline, List<String> tags) {
        if (this.status != ChallengeStatus.DRAFT) {
            throw new IllegalStateException("Modification impossible : le défi n'est plus en statut DRAFT.");
        }
        if (title != null && title.length() > 140) {
            throw new IllegalArgumentException("Le titre ne peut pas dépasser 140 caractères.");
        }
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (deadline != null) this.deadline = deadline;
        if (tags != null) this.tags = new ArrayList<>(tags);
    }

    public void cancel(String reason) {
        this.status = ChallengeStatus.CANCELLED;
    }

    public void addCriterion(AcceptanceCriterion criterion) {
        if (this.acceptanceCriteria == null) {
            this.acceptanceCriteria = new ArrayList<>();
        }
        this.acceptanceCriteria.add(criterion);
    }

    public void setPrizesList(List<Prize> newPrizes) {
        this.prizes = newPrizes != null ? new ArrayList<>(newPrizes) : new ArrayList<>();
    }
}
