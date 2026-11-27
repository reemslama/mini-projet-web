package tn.arene.ms1challenges.actuator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import tn.arene.ms1challenges.repositories.ChallengeRepository;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * HealthIndicator personnalise pour MS1 Challenges (Atelier Observabilite).
 * Fait remonter la sante metier reelle du microservice dans /actuator/health et Eureka.
 */
@Component("challengeCustomHealth")
@RequiredArgsConstructor
public class ChallengeCustomHealthIndicator implements HealthIndicator {

    private final ChallengeRepository challengeRepository;
    private final AtomicBoolean simulatedDown = new AtomicBoolean(false);

    public void setSimulatedDown(boolean down) {
        this.simulatedDown.set(down);
    }

    public boolean isSimulatedDown() {
        return this.simulatedDown.get();
    }

    @Override
    public Health health() {
        // Verification de la simulation de panne pour l'atelier
        if (simulatedDown.get()) {
            return Health.down()
                    .withDetail("service", "MS1-Challenges")
                    .withDetail("reason", "Panne simulee via HealthIndicator pour l'Atelier Actuator")
                    .withDetail("help", "Appelez POST /api/challenges/actuator/simulate-up pour retablir")
                    .build();
        }

        try {
            long totalChallenges = challengeRepository.count();
            return Health.up()
                    .withDetail("service", "MS1-Challenges")
                    .withDetail("database", "H2 (accessible)")
                    .withDetail("totalChallenges", totalChallenges)
                    .withDetail("status", "Operationnel")
                    .build();
        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("service", "MS1-Challenges")
                    .withDetail("error", "Impossible de joindre la base H2: " + e.getMessage())
                    .build();
        }
    }
}
