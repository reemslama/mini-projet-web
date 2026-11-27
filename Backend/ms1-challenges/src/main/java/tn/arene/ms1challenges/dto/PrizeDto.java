package tn.arene.ms1challenges.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.arene.ms1challenges.entities.Prize;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrizeDto {

    @NotNull(message = "Le rang est obligatoire")
    @Min(value = 1, message = "Le rang minimum est 1")
    @Max(value = 3, message = "Le rang maximum est 3 (Top 3)")
    private Integer rank;

    @NotNull(message = "Le montant est obligatoire")
    private Double amount;

    @Builder.Default
    private String currency = "EUR";

    public static PrizeDto fromEntity(Prize prize) {
        if (prize == null) return null;
        return PrizeDto.builder()
                .rank(prize.getRank())
                .amount(prize.getAmount())
                .currency(prize.getCurrency())
                .build();
    }

    public Prize toEntity() {
        return Prize.builder()
                .rank(this.rank != null ? this.rank : 1)
                .amount(this.amount != null ? this.amount : 0.0)
                .currency(this.currency != null ? this.currency : "EUR")
                .build();
    }
}
