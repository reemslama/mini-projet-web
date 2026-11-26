package tn.arene.ms4evaluation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.arene.ms4evaluation.entities.Jury;

import java.util.UUID;

public interface JuryRepository extends JpaRepository<Jury, UUID> {
}