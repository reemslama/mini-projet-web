
package tn.arene.ms4evaluation.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RubricCriterion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    String label;

    double weight;

    double maxScore;

    @ManyToOne
    @JsonBackReference
    Rubric rubric;
}
