
package tn.arene.ms4evaluation.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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
public class Rubric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    UUID challengeId;

    String name;

    @OneToMany(mappedBy = "rubric",
            cascade = CascadeType.ALL)
    @JsonManagedReference
    @Builder.Default
    List<RubricCriterion> criteria = new ArrayList<>();
}
