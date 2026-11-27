package tn.esprit.arene.participants.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Petite route de démonstration : montre QUELLE instance répond.
 * Utile pour prouver qu'on a bien 2 instances (8082 et 8092).
 */
@RestController
public class InstanceController {

    @Value("${spring.application.name}")
    private String nom;

    @Value("${server.port}")
    private String port;

    @GetMapping("/api/participants/hello")
    public Map<String, String> hello() {
        return Map.of("service", nom, "port", port, "message", "Bonjour depuis MS2 Participants");
    }
}
