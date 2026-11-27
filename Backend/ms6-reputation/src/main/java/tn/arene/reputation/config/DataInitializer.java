package tn.arene.reputation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.arene.reputation.dto.ProfileRequest;
import tn.arene.reputation.dto.ReputationEventRequest;
import tn.arene.reputation.entities.ReputationEventType;
import tn.arene.reputation.repositories.ReputationProfileRepository;
import tn.arene.reputation.services.IReputationService;

import java.util.List;
import java.util.UUID;

/** Donnees de demo chargees au demarrage (H2 en memoire). UUIDs fixes pour les tests .http. */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    public static final UUID DEV_AMINE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID DEV_SARA = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CHALLENGE_1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private final IReputationService service;
    private final ReputationProfileRepository profiles;

    @Override
    public void run(String... args) {
        if (profiles.count() > 0) return;
        service.createProfile(new ProfileRequest(DEV_AMINE, "amine.dev", List.of("java", "spring")));
        service.createProfile(new ProfileRequest(DEV_SARA, "sara.codes", List.of("angular", "java")));
        service.ingest(new ReputationEventRequest(ReputationEventType.SUBMISSION_PASSED, DEV_AMINE, CHALLENGE_1, null, null));
        service.ingest(new ReputationEventRequest(ReputationEventType.CHALLENGE_WON, DEV_AMINE, CHALLENGE_1, null, null));
        service.ingest(new ReputationEventRequest(ReputationEventType.SUBMISSION_PASSED, DEV_SARA, CHALLENGE_1, null, null));
    }
}
