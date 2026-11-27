package tn.arene.ms1challenges.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.arene.ms1challenges.entities.AcceptanceCriterion;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcceptanceCriterionDto {

    @NotBlank(message = "L'identifiant du critère est obligatoire")
    private String id;

    @NotBlank(message = "La description du critère est obligatoire")
    private String description;

    @NotNull(message = "Le poids (weight) est obligatoire")
    @DecimalMin(value = "0.0", message = "Le poids doit être >= 0.0")
    @DecimalMax(value = "1.0", message = "Le poids doit être <= 1.0")
    private Double weight;

    private String testCommand;

    public static AcceptanceCriterionDto fromEntity(AcceptanceCriterion entity) {
        if (entity == null) return null;
        return AcceptanceCriterionDto.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .weight(entity.getWeight())
                .testCommand(entity.getTestCommand())
                .build();
    }

    public AcceptanceCriterion toEntity() {
        return AcceptanceCriterion.builder()
                .id(this.id)
                .description(this.description)
                .weight(this.weight != null ? this.weight : 0.0)
                .testCommand(this.testCommand)
                .build();
    }
}
