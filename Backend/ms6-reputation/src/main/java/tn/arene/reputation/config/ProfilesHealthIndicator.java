package tn.arene.reputation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import tn.arene.reputation.repositories.ReputationProfileRepository;

/**
 * HealthIndicator personnalise (atelier Actuator) : composant "reputationStore" dans /actuator/health.
 * Verifie que la base MS6 repond et publie le nombre de profils.
 */
@Component("reputationStore")
@RequiredArgsConstructor
public class ProfilesHealthIndicator implements HealthIndicator {

    private final ReputationProfileRepository profiles;

    @Override
    public Health health() {
        try {
            return Health.up().withDetail("profiles", profiles.count()).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
