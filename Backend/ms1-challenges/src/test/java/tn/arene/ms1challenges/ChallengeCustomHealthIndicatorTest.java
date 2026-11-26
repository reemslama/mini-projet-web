package tn.arene.ms1challenges;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import tn.arene.ms1challenges.actuator.ChallengeCustomHealthIndicator;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChallengeCustomHealthIndicatorTest {

    @Autowired
    private ChallengeCustomHealthIndicator customHealthIndicator;

    @Test
    @DisplayName("HealthIndicator doit renvoyer UP quand aucune panne n'est simulee")
    void testHealthUp() {
        customHealthIndicator.setSimulatedDown(false);
        Health health = customHealthIndicator.health();
        assertEquals(Status.UP, health.getStatus());
        assertEquals("MS1-Challenges", health.getDetails().get("service"));
    }

    @Test
    @DisplayName("HealthIndicator doit renvoyer DOWN quand une panne est simulee")
    void testHealthDown() {
        customHealthIndicator.setSimulatedDown(true);
        Health health = customHealthIndicator.health();
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("MS1-Challenges", health.getDetails().get("service"));

        // Retablir a la fin du test
        customHealthIndicator.setSimulatedDown(false);
    }
}
