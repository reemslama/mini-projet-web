package tn.arene.ms4evaluation.entities;

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
public class Jury {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    UUID challengeId;

    @ManyToMany
    @Builder.Default
    Set<Expert> experts = new HashSet<>();
}