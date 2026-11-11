package com.enlice.evaluation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "winners")
public class Winner {

    @Id
    @Column(name = "challenge_id", nullable = false)
    private UUID challengeId;

    @Column(name = "rank", nullable = false)
    private Integer rank;

    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;

    @Column(name = "applicant_id", nullable = false)
    private UUID applicantId;

    @Column(name = "score", nullable = false)
    private double score;

    @Column(name = "prize_amount", nullable = false)
    private double prizeAmount;

    protected Winner() {
    }

    public Winner(UUID challengeId, Integer rank, UUID submissionId, UUID applicantId, double score,
                  double prizeAmount) {
        this.challengeId = challengeId;
        this.rank = rank;
        this.submissionId = submissionId;
        this.applicantId = applicantId;
        this.score = score;
        this.prizeAmount = prizeAmount;
    }

    public UUID getChallengeId() { return challengeId; }
    public void setChallengeId(UUID challengeId) { this.challengeId = challengeId; }
    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public UUID getSubmissionId() { return submissionId; }
    public void setSubmissionId(UUID submissionId) { this.submissionId = submissionId; }
    public UUID getApplicantId() { return applicantId; }
    public void setApplicantId(UUID applicantId) { this.applicantId = applicantId; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public double getPrizeAmount() { return prizeAmount; }
    public void setPrizeAmount(double prizeAmount) { this.prizeAmount = prizeAmount; }
}
