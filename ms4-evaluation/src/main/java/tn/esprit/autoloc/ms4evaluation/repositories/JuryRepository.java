package tn.esprit.autoloc.ms4evaluation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.ms4evaluation.entities.Jury;

import java.util.UUID;

public interface JuryRepository extends JpaRepository<Jury, UUID> {
}