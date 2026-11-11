package com.enlice.challenges.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/** Prime attribuée au rang 1..3 d'un défi. */
@Entity
@Table(name = "prizes")
public class Prize {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "challenge_id", nullable = false)
    private UUID challengeId;

    @Column(name = "rank", nullable = false)
    private int rank;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "currency", nullable = false, length = 8)
    private String currency = "EUR";

    protected Prize() {
    }

    public Prize(UUID id, UUID challengeId, int rank, double amount, String currency) {
        this.id = id;
        this.challengeId = challengeId;
        this.rank = rank;
        this.amount = amount;
        this.currency = currency == null || currency.isBlank() ? "EUR" : currency;
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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
