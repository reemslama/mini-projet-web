package tn.esprit.arene.participants.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.arene.participants.entity.Niveau;
import tn.esprit.arene.participants.entity.Utilisateur;

import java.util.List;
import java.util.UUID;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPseudoIgnoreCase(String pseudo);

    List<Utilisateur> findByNiveau(Niveau niveau);

    List<Utilisateur> findByDisponibleTrue();
}
