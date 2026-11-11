package com.enlice.submissions.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Verdict de vérification produit par MS5 et consommé par MS3. */
@Entity
@Table(name = "verdicts")
public class Verdict {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "submission_id", nullable = false, unique = true)
    private UUID submissionId;

    @Column(name = "result", nullable = false, length = 8)
    private String result;

    @Column(name = "passed")
    private Integer passed;

    @Column(name = "failed")
    private Integer failed;

    @Column(name = "total")
    private Integer total;

    @Column(name = "coverage")
    private Double coverage;

    @Column(name = "logs_url", length = 512)
    private String logsUrl;

    @Column(name = "verified_at", nullable = false)
    private Instant verifiedAt;

    protected Verdict() {
    }

    public Verdict(UUID submissionId, String result, Integer passed, Integer failed, Integer total, Double coverage,
                   String logsUrl, Instant verifiedAt) {
        this.id = UUID.randomUUID();
        this.submissionId = submissionId;
        this.result = result;
        this.passed = passed;
        this.failed = failed;
        this.total = total;
        this.coverage = coverage;
        this.logsUrl = logsUrl;
        this.verifiedAt = verifiedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(UUID submissionId) {
        this.submissionId = submissionId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getPassed() {
        return passed;
    }

    public void setPassed(Integer passed) {
        this.passed = passed;
    }

    public Integer getFailed() {
        return failed;
    }

    public void setFailed(Integer failed) {
        this.failed = failed;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Double getCoverage() {
        return coverage;
    }

    public void setCoverage(Double coverage) {
        this.coverage = coverage;
    }

    public String getLogsUrl() {
        return logsUrl;
    }

    public void setLogsUrl(String logsUrl) {
        this.logsUrl = logsUrl;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
