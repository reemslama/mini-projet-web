package com.enlice.evaluation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Embeddable
public class Score {

    private String criterionLabel;
    private double score;

    protected Score() {
    }

    public Score(String criterionLabel, double score) {
        this.criterionLabel = criterionLabel;
        this.score = score;
    }

    public String getCriterionLabel() { return criterionLabel; }
    public void setCriterionLabel(String criterionLabel) { this.criterionLabel = criterionLabel; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}
