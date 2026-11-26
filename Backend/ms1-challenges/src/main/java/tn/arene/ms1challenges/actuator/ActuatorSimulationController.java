package tn.arene.ms1challenges.actuator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controleur d'assistance pour tester l'atelier Actuator :
 * Permet de basculer la sante du service UP <-> DOWN et d'observer
 * la propagation automatique du statut dans le tableau de bord Eureka.
 */
@RestController
@RequestMapping("/api/challenges/actuator")
@RequiredArgsConstructor
@Tag(name = "Actuator Simulation", description = "Endpoints de test pour simuler une panne et tester le statut Eureka")
public class ActuatorSimulationController {

    private final ChallengeCustomHealthIndicator customHealthIndicator;

    @Operation(summary = "Simuler une panne", description = "Fait passer /actuator/health en statut DOWN (HTTP 503) et met a jour Eureka")
    @PostMapping("/simulate-down")
    public ResponseEntity<Map<String, Object>> simulateDown() {
        customHealthIndicator.setSimulatedDown(true);
        return ResponseEntity.ok(Map.of(
                "message", "Panne simulee activee. Verifiez http://localhost:8081/actuator/health et http://localhost:8761",
                "simulatedDown", true
        ));
    }

    @Operation(summary = "Retablir le service", description = "Fait repasser /actuator/health en statut UP (HTTP 200) et met a jour Eureka")
    @PostMapping("/simulate-up")
    public ResponseEntity<Map<String, Object>> simulateUp() {
        customHealthIndicator.setSimulatedDown(false);
        return ResponseEntity.ok(Map.of(
                "message", "Service retabli. Verifiez http://localhost:8081/actuator/health et http://localhost:8761",
                "simulatedDown", false
        ));
    }

    @Operation(summary = "Etat de la simulation", description = "Consulte l'etat actuel de la simulation de panne")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "simulatedDown", customHealthIndicator.isSimulatedDown(),
                "service", "MS1-Challenges"
        ));
    }
}
