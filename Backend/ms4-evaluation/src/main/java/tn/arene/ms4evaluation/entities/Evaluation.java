
package tn.arene.ms4evaluation.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    UUID submissionId;

    String comment;

    double weightedScore;

    LocalDateTime evaluatedAt;

    @ManyToOne
    Expert expert;

    @ManyToOne
    Rubric rubric;

    @OneToMany(mappedBy = "evaluation",
            cascade = CascadeType.ALL)
    @JsonManagedReference
    @Builder.Default
    List<Score> scores = new ArrayList<>();
}
