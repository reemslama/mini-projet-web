package com.enlice.evaluation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "evaluations")
public class Evaluation {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;

    @Column(name = "challenge_id", nullable = false)
    private UUID challengeId;

    @Column(name = "expert_id", nullable = false)
    private UUID expertId;

    @Column(name = "rubric_id", nullable = false)
    private UUID rubricId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "evaluation_scores", joinColumns = @JoinColumn(name = "evaluation_id"))
    @Column(name = "criterion_label", nullable = false)
    private List<Score> scores = new ArrayList<>();

    private String comment;

    @Column(name = "weighted_score", nullable = false)
    private double weightedScore;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    protected Evaluation() {
    }

    public Evaluation(UUID submissionId, UUID challengeId, UUID expertId, UUID rubricId, List<Score> scores,
                       String comment, double weightedScore, Instant evaluatedAt) {
        this.id = UUID.randomUUID();
        this.submissionId = submissionId;
        this.challengeId = challengeId;
        this.expertId = expertId;
        this.rubricId = rubricId;
        this.scores = scores == null ? new ArrayList<>() : new ArrayList<>(scores);
        this.comment = comment;
        this.weightedScore = weightedScore;
        this.evaluatedAt = evaluatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSubmissionId() { return submissionId; }
    public void setSubmissionId(UUID submissionId) { this.submissionId = submissionId; }
    public UUID getChallengeId() { return challengeId; }
    public void setChallengeId(UUID challengeId) { this.challengeId = challengeId; }
    public UUID getExpertId() { return expertId; }
    public void setExpertId(UUID expertId) { this.expertId = expertId; }
    public UUID getRubricId() { return rubricId; }
    public void setRubricId(UUID rubricId) { this.rubricId = rubricId; }
    public List<Score> getScores() { return scores; }
    public void setScores(List<Score> scores) { this.scores = scores == null ? new ArrayList<>() : new ArrayList<>(scores); }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public double getWeightedScore() { return weightedScore; }
    public void setWeightedScore(double weightedScore) { this.weightedScore = weightedScore; }
    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
