package tn.arene.ms4evaluation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.arene.ms4evaluation.entities.Expert;

import java.util.UUID;

public interface ExpertRepository extends JpaRepository<Expert, UUID> {
}