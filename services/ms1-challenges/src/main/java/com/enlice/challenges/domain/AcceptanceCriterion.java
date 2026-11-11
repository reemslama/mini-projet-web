package com.enlice.challenges.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/** Critère d'acceptation (spec de tests exécutée par MS5). */
@Entity
@Table(name = "acceptance_criteria")
public class AcceptanceCriterion {

    @Id
    @Column(name = "id", nullable = false, length = 80)
    private String id;

    @Column(name = "challenge_id", nullable = false)
    private UUID challengeId;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "weight", nullable = false)
    private double weight;

    @Column(name = "test_command", length = 1000)
    private String testCommand;

    protected AcceptanceCriterion() {
    }

    public AcceptanceCriterion(String id, UUID challengeId, String description, double weight, String testCommand) {
        this.id = id;
        this.challengeId = challengeId;
        this.description = description;
        this.weight = weight;
        this.testCommand = testCommand;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UUID getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(UUID challengeId) {
        this.challengeId = challengeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getTestCommand() {
        return testCommand;
    }

    public void setTestCommand(String testCommand) {
        this.testCommand = testCommand;
    }
}
