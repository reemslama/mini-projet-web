package tn.esprit.arene.participants.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import tn.esprit.arene.participants.entity.StatutCandidature;
import tn.esprit.arene.participants.repository.CandidatureRepository;
import tn.esprit.arene.participants.repository.EquipeRepository;
import tn.esprit.arene.participants.repository.UtilisateurRepository;

/**
 * HealthIndicator personnalisé (atelier Observabilité) :
 * apparaît dans /actuator/health sous le nom "participants".
 * Si la base ne répond plus, le composant passe DOWN -> le service passe DOWN dans Eureka.
 */
@Component("participants")
public class ParticipantsHealthIndicator implements HealthIndicator {

    private final UtilisateurRepository utilisateurRepository;
    private final EquipeRepository equipeRepository;
    private final CandidatureRepository candidatureRepository;

    public ParticipantsHealthIndicator(UtilisateurRepository utilisateurRepository,
                                       EquipeRepository equipeRepository,
                                       CandidatureRepository candidatureRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.equipeRepository = equipeRepository;
        this.candidatureRepository = candidatureRepository;
    }

    @Override
    public Health health() {
        try {
            return Health.up()
                    .withDetail("utilisateurs", utilisateurRepository.count())
                    .withDetail("equipes", equipeRepository.count())
                    .withDetail("candidatures", candidatureRepository.count())
                    .withDetail("candidaturesEnAttente", candidatureRepository.countByStatut(StatutCandidature.EN_ATTENTE))
                    .build();
        } catch (Exception e) {
            return Health.down(e).withDetail("raison", "Base Participants inaccessible").build();
        }
    }
}
