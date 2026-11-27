package tn.esprit.arene.participants.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.arene.participants.entity.Equipe;

import java.util.UUID;

public interface EquipeRepository extends JpaRepository<Equipe, UUID> {

    boolean existsByNomIgnoreCase(String nom);

    boolean existsByChefId(UUID chefId);
}
