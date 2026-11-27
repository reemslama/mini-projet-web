package tn.arene.ms1challenges.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "acceptance_criteria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcceptanceCriterion {

    @Id
    private String id;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private double weight;

    @Column(name = "test_command", length = 1000)
    private String testCommand;

    public boolean hasTestCommand() {
        return testCommand != null && !testCommand.trim().isEmpty();
    }

    public boolean validateWeight() {
        return weight >= 0.0 && weight <= 1.0;
    }
}
