package tn.esprit.arene.participants.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.arene.participants.entity.MembreEquipe;

import java.util.List;
import java.util.UUID;

public interface MembreEquipeRepository extends JpaRepository<MembreEquipe, UUID> {

    /** Les adhésions d'un utilisateur : "dans quelles équipes suis-je ?" */
    List<MembreEquipe> findByUtilisateurId(UUID utilisateurId);

    boolean existsByUtilisateurId(UUID utilisateurId);
}
