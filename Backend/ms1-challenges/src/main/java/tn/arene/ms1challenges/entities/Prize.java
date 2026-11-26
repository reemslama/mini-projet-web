package tn.arene.ms1challenges.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prize {

    @Column(nullable = false)
    private int rank;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "EUR";

    public boolean isTopThree() {
        return rank >= 1 && rank <= 3;
    }

    public String formattedAmount() {
        return (amount != null ? amount.toString() : "0") + " " + (currency != null ? currency : "EUR");
    }
}
