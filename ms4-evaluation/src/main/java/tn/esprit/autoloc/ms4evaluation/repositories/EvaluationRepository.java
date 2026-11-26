package tn.esprit.autoloc.ms4evaluation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.ms4evaluation.entities.Evaluation;

import java.util.UUID;

public interface EvaluationRepository extends JpaRepository<Evaluation, UUID> {
}