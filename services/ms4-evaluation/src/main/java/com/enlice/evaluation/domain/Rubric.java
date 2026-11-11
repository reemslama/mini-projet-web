package com.enlice.evaluation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rubrics")
public class Rubric {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "challenge_id", nullable = false)
    private UUID challengeId;

    @Column(name = "name", nullable = false, length = 140)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rubric_criteria", joinColumns = @JoinColumn(name = "rubric_id"))
    @Column(name = "criterion", nullable = false)
    private List<Criterion> criteria = new ArrayList<>();

    protected Rubric() {
    }

    public Rubric(UUID id, UUID challengeId, String name, List<Criterion> criteria) {
        this.id = id;
        this.challengeId = challengeId;
        this.name = name;
        this.criteria = criteria == null ? new ArrayList<>() : new ArrayList<>(criteria);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getChallengeId() { return challengeId; }
    public void setChallengeId(UUID challengeId) { this.challengeId = challengeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Criterion> getCriteria() { return criteria; }
    public void setCriteria(List<Criterion> criteria) { this.criteria = criteria == null ? new ArrayList<>() : new ArrayList<>(criteria); }
}
