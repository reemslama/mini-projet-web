package tn.esprit.autoloc.ms4evaluation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.ms4evaluation.entities.RubricCriterion;

import java.util.UUID;

public interface RubricCriterionRepository extends JpaRepository<RubricCriterion, UUID> {
}