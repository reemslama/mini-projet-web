package com.enlice.submissions.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "challenge_id", nullable = false)
    private UUID challengeId;

    @Column(name = "applicant_id", nullable = false)
    private UUID applicantId;

    @Column(name = "repo_url", nullable = false, length = 512)
    private String repoUrl;

    @Column(name = "branch", nullable = false, length = 64)
    private String branch = "main";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubmissionStatus status = SubmissionStatus.DRAFT;

    @Column(name = "commit_sha", length = 64)
    private String commitSha;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "similarity_flagged")
    private boolean similarityFlagged;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Submission() {
    }

    public Submission(UUID challengeId, UUID applicantId, String repoUrl, String branch, SubmissionStatus status,
                       String commitSha, Instant submittedAt, boolean similarityFlagged, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.challengeId = challengeId;
        this.applicantId = applicantId;
        this.repoUrl = repoUrl;
        this.branch = branch == null ? "main" : branch;
        this.status = status;
        this.commitSha = commitSha;
        this.submittedAt = submittedAt;
        this.similarityFlagged = similarityFlagged;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(UUID challengeId) {
        this.challengeId = challengeId;
    }

    public UUID getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(UUID applicantId) {
        this.applicantId = applicantId;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public boolean isSimilarityFlagged() {
        return similarityFlagged;
    }

    public void setSimilarityFlagged(boolean similarityFlagged) {
        this.similarityFlagged = similarityFlagged;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
