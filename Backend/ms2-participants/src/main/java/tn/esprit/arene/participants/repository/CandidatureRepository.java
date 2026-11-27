package tn.esprit.arene.participants.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.arene.participants.entity.Candidature;
import tn.esprit.arene.participants.entity.StatutCandidature;

import java.util.List;
import java.util.UUID;

public interface CandidatureRepository extends JpaRepository<Candidature, UUID> {

    List<Candidature> findByChallengeId(UUID challengeId);

    List<Candidature> findByDeposantId(UUID utilisateurId);

    List<Candidature> findByEquipeId(UUID equipeId);

    long countByStatut(StatutCandidature statut);

    /** Doublon : même utilisateur en SOLO sur le même défi. */
    boolean existsByChallengeIdAndDeposantIdAndEquipeIsNull(UUID challengeId, UUID deposantId);

    /** Doublon : même équipe sur le même défi. */
    boolean existsByChallengeIdAndEquipeId(UUID challengeId, UUID equipeId);

    boolean existsByDeposantId(UUID deposantId);

    boolean existsByEquipeId(UUID equipeId);
}
