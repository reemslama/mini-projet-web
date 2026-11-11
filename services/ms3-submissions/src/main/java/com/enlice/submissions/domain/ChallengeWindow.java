package com.enlice.submissions.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Fenêtre temporelle d'un défi, alimentée par {@code challenge.published}. */
@Entity
@Table(name = "challenge_windows")
public class ChallengeWindow {

    @Id
    @Column(name = "challenge_id", nullable = false)
    private UUID challengeId;

    @Column(name = "deadline", nullable = false)
    private Instant deadline;

    protected ChallengeWindow() {
    }

    public ChallengeWindow(UUID challengeId, Instant deadline) {
        this.challengeId = challengeId;
        this.deadline = deadline;
    }

    public UUID getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(UUID challengeId) {
        this.challengeId = challengeId;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public void setDeadline(Instant deadline) {
        this.deadline = deadline;
    }
}
