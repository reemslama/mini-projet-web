package com.enlice.challenges.config;

import com.enlice.challenges.domain.AcceptanceCriterion;
import com.enlice.challenges.domain.AcceptanceCriterionRepository;
import com.enlice.challenges.domain.Challenge;
import com.enlice.challenges.domain.ChallengeRepository;
import com.enlice.challenges.domain.ChallengeStatus;
import com.enlice.challenges.domain.Prize;
import com.enlice.challenges.domain.PrizeRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Jeu de démonstration idempotent (désactivable via {@code enlice.seed.enabled=false}).
 */
@Component
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private static final UUID ENTERPRISE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID CHALLENGE_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CHALLENGE_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CHALLENGE_3 = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private final ChallengeRepository challengeRepository;
    private final AcceptanceCriterionRepository criterionRepository;
    private final PrizeRepository prizeRepository;
    private final boolean seedEnabled;

    public DemoDataSeeder(ChallengeRepository challengeRepository,
                          AcceptanceCriterionRepository criterionRepository,
                          PrizeRepository prizeRepository,
                          @Value("${enlice.seed.enabled:true}") boolean seedEnabled) {
        this.challengeRepository = challengeRepository;
        this.criterionRepository = criterionRepository;
        this.prizeRepository = prizeRepository;
        this.seedEnabled = seedEnabled;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            log.info("Seed désactivé (enlice.seed.enabled=false)");
            return;
        }
        if (challengeRepository.count() > 0) {
            log.info("Seed ignoré : des défis existent déjà");
            return;
        }
        Instant deadline = Instant.now().plus(60, ChronoUnit.DAYS);

        createChallenge(CHALLENGE_1, "Plateforme de réservation verte",
                "Concevoir une API de réservation bas-carbone pour les flottes d'entreprise.",
                Set.of("java", "spring", "api"), deadline);
        createChallenge(CHALLENGE_2, "Tableau de bord énergie",
                "Développer un tableau de bord temps réel de consommation énergétique multi-sites.",
                Set.of("angular", "typescript", "dataviz"), deadline);
        createChallenge(CHALLENGE_3, "Assistant de tri automatique",
                "Classifier automatiquement les déchets d'un site industriel à partir d'images.",
                Set.of("python", "ia", "computer-vision"), deadline);

        log.info("Seed MS1 terminé : 3 défis publiés");
    }

    private void createChallenge(UUID id, String title, String description, Set<String> tags, Instant deadline) {
        Challenge challenge = new Challenge(id, title, description, ENTERPRISE_ID, deadline,
                new LinkedHashSet<>(tags), Instant.now());
        challenge.setStatus(ChallengeStatus.PUBLISHED);
        challenge.setPublishedAt(Instant.now());
        challengeRepository.save(challenge);

        criterionRepository.saveAll(List.of(
                new AcceptanceCriterion(UUID.randomUUID().toString(), id,
                        "Le projet compile et la suite de tests fournie passe", 0.6, "mvn -B test"),
                new AcceptanceCriterion(UUID.randomUUID().toString(), id,
                        "La documentation d'installation permet un démarrage en moins de 10 minutes", 0.4,
                        "mvn -B verify")));

        prizeRepository.saveAll(List.of(
                new Prize(UUID.randomUUID(), id, 1, 5000, "EUR"),
                new Prize(UUID.randomUUID(), id, 2, 2500, "EUR"),
                new Prize(UUID.randomUUID(), id, 3, 1000, "EUR")));
    }
}
