
package tn.esprit.autoloc.ms4evaluation.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Winner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    UUID challengeId;

    UUID submissionId;

    UUID applicantId;

    int rank;

    double score;

    BigDecimal prizeAmount;
}
