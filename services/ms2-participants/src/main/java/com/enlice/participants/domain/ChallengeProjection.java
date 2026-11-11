package com.enlice.participants.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Projection du défi utilisée par MS2 (deadline / statut) pour valider une candidature. */
@Entity
@Table(name = "challenge_projections")
public class ChallengeProjection {

    @Id
    @Column(name = "challenge_id", nullable = false)
    private UUID challengeId;

    @Column(name = "deadline", nullable = false)
    private Instant deadline;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "application_count", nullable = false)
    private int applicationCount;

    protected ChallengeProjection() {
    }

    public ChallengeProjection(UUID challengeId, Instant deadline, String status, Instant publishedAt,
                               int applicationCount) {
        this.challengeId = challengeId;
        this.deadline = deadline;
        this.status = status;
        this.publishedAt = publishedAt;
        this.applicationCount = applicationCount;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public int getApplicationCount() {
        return applicationCount;
    }

    public void setApplicationCount(int applicationCount) {
        this.applicationCount = applicationCount;
    }
}
