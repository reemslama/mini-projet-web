package com.enlice.evaluation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Embeddable
public class Criterion {

    private String label;
    private double weight;
    private double maxScore;

    protected Criterion() {
    }

    public Criterion(String label, double weight, double maxScore) {
        this.label = label;
        this.weight = weight;
        this.maxScore = maxScore;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }
}
