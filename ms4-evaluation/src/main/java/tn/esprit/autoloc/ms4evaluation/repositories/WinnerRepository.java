package tn.esprit.autoloc.ms4evaluation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.ms4evaluation.entities.Winner;

import java.util.UUID;

public interface WinnerRepository extends JpaRepository<Winner, UUID> {
}