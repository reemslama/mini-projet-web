package com.enlice.evaluation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "juries")
public class Jury {

    @Id
    @Column(name = "challenge_id", nullable = false)
    private UUID challengeId;

    @Column(name = "expert_ids", nullable = false, length = 1000)
    private String expertIds;

    protected Jury() {
    }

    public Jury(UUID challengeId, Set<UUID> expertIds) {
        this.challengeId = challengeId;
        this.expertIds = expertIds == null ? "" : expertIds.stream().map(UUID::toString).reduce((a, b) -> a + "," + b).orElse("");
    }

    public UUID getChallengeId() { return challengeId; }
    public void setChallengeId(UUID challengeId) { this.challengeId = challengeId; }
    public String getExpertIds() { return expertIds; }
    public void setExpertIds(String expertIds) { this.expertIds = expertIds; }
}
