package tn.arene.ms4evaluation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.arene.ms4evaluation.entities.Score;

import java.util.UUID;

public interface ScoreRepository extends JpaRepository<Score, UUID> {
}