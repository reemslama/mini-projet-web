
package tn.esprit.autoloc.ms4evaluation.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Expert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    String keycloakId;

    String displayName;

    @ElementCollection
    @Builder.Default
    Set<String> domains = new HashSet<>();
}
