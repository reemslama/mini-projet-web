package tn.esprit.autoloc.ms4evaluation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.ms4evaluation.entities.Rubric;

import java.util.UUID;

public interface RubricRepository extends JpaRepository<Rubric, UUID> {
}