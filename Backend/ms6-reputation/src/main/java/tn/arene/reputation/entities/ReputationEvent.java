package tn.arene.reputation.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evenement agrege dans un profil (ManyToOne vers ReputationProfile).
 * Idempotence : (type, developerId, challengeId) unique, un evenement recu deux fois ne compte qu'une fois.
 */
@Entity
@Table(name = "reputation_event",
        uniqueConstraints = @UniqueConstraint(columnNames = {"type", "developer_id", "challenge_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ReputationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReputationEventType type;

    @Column(name = "developer_id", nullable = false)
    private UUID developerId;

    /** Reference MS1 par identifiant. */
    @Column(name = "challenge_id")
    private UUID challengeId;

    private int weight;

    private LocalDateTime occurredAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id")
    @JsonIgnore
    private ReputationProfile profile;

    public static ReputationEvent of(ReputationEventType type, UUID developerId, UUID challengeId,
                                     Integer weight, LocalDateTime occurredAt) {
        ReputationEvent e = new ReputationEvent();
        e.type = type;
        e.developerId = developerId;
        e.challengeId = challengeId;
        e.weight = weight != null ? weight : type.getDefaultWeight();
        e.occurredAt = occurredAt != null ? occurredAt : LocalDateTime.now();
        return e;
    }
}
