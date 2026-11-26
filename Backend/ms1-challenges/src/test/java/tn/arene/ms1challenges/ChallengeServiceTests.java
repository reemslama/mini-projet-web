package tn.arene.ms1challenges;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import tn.arene.ms1challenges.dto.AcceptanceCriterionDto;
import tn.arene.ms1challenges.dto.ChallengeCreateRequest;
import tn.arene.ms1challenges.dto.ChallengeResponse;
import tn.arene.ms1challenges.dto.PrizeDto;
import tn.arene.ms1challenges.entities.ChallengeStatus;
import tn.arene.ms1challenges.exceptions.ConflictException;
import tn.arene.ms1challenges.repositories.ChallengeRepository;
import tn.arene.ms1challenges.services.IChallengeService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChallengeServiceTests {

    @Autowired
    private IChallengeService challengeService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @BeforeEach
    void setup() {
        challengeRepository.deleteAll();
    }

    @Test
    @DisplayName("Doit créer un défi avec le statut DRAFT et les règles de base")
    void testCreateChallenge() {
        UUID enterpriseId = UUID.randomUUID();
        ChallengeCreateRequest req = ChallengeCreateRequest.builder()
                .title("Défi Algo Graphes")
                .description("Implémentation de l'algorithme A*")
                .enterpriseId(enterpriseId)
                .deadline(LocalDateTime.now().plusDays(10))
                .tags(List.of("java", "algo"))
                .build();

        ChallengeResponse created = challengeService.createChallenge(req);

        assertNotNull(created.getId());
        assertEquals("Défi Algo Graphes", created.getTitle());
        assertEquals(ChallengeStatus.DRAFT, created.getStatus());
        assertNotNull(created.getCreatedAt());
        assertNull(created.getPublishedAt());
    }

    @Test
    @DisplayName("Doit publier un défi DRAFT et alimenter publishedAt")
    void testPublishChallenge() {
        ChallengeCreateRequest req = ChallengeCreateRequest.builder()
                .title("Défi Microservices")
                .description("Conception d'un bus événementiel")
                .enterpriseId(UUID.randomUUID())
                .deadline(LocalDateTime.now().plusDays(5))
                .tags(List.of("spring-cloud"))
                .build();

        ChallengeResponse created = challengeService.createChallenge(req);
        ChallengeResponse published = challengeService.publishChallenge(created.getId());

        assertEquals(ChallengeStatus.PUBLISHED, published.getStatus());
        assertNotNull(published.getPublishedAt());

        // Une deuxième publication doit lever un conflit
        assertThrows(ConflictException.class, () -> challengeService.publishChallenge(created.getId()));
    }

    @Test
    @DisplayName("Doit refuser la modification si le défi est déjà publié")
    void testRefuseUpdateWhenNotDraft() {
        ChallengeCreateRequest req = ChallengeCreateRequest.builder()
                .title("Défi Initial")
                .description("Description initiale")
                .enterpriseId(UUID.randomUUID())
                .deadline(LocalDateTime.now().plusDays(2))
                .build();

        ChallengeResponse created = challengeService.createChallenge(req);
        challengeService.publishChallenge(created.getId());

        ChallengeCreateRequest updateReq = ChallengeCreateRequest.builder()
                .title("Nouveau titre")
                .description("Nouvelle description")
                .enterpriseId(created.getEnterpriseId())
                .deadline(LocalDateTime.now().plusDays(3))
                .build();

        assertThrows(ConflictException.class, () -> challengeService.updateChallenge(created.getId(), updateReq));
    }

    @Test
    @DisplayName("Doit enregistrer et récupérer les critères d'acceptation")
    void testAcceptanceCriteria() {
        ChallengeResponse created = challengeService.createChallenge(ChallengeCreateRequest.builder()
                .title("Défi Tests")
                .description("Vérification des critères")
                .enterpriseId(UUID.randomUUID())
                .deadline(LocalDateTime.now().plusDays(7))
                .build());

        List<AcceptanceCriterionDto> criteria = List.of(
                AcceptanceCriterionDto.builder().id("C1").description("Tests unitaires passants").weight(0.4).testCommand("mvn test").build(),
                AcceptanceCriterionDto.builder().id("C2").description("Couverture > 80%").weight(0.6).testCommand("mvn jacoco:report").build()
        );

        List<AcceptanceCriterionDto> updated = challengeService.updateAcceptanceCriteria(created.getId(), criteria);
        assertEquals(2, updated.size());

        List<AcceptanceCriterionDto> fetched = challengeService.getAcceptanceCriteria(created.getId());
        assertEquals(2, fetched.size());
        assertEquals("mvn test", fetched.get(0).getTestCommand());
    }

    @Test
    @DisplayName("Doit enregistrer les primes pour le Top 3")
    void testPrizes() {
        ChallengeResponse created = challengeService.createChallenge(ChallengeCreateRequest.builder()
                .title("Défi Doté")
                .description("Primes du podium")
                .enterpriseId(UUID.randomUUID())
                .deadline(LocalDateTime.now().plusDays(15))
                .build());

        List<PrizeDto> prizes = List.of(
                PrizeDto.builder().rank(1).amount(3000.0).currency("EUR").build(),
                PrizeDto.builder().rank(2).amount(1500.0).currency("EUR").build(),
                PrizeDto.builder().rank(3).amount(500.0).currency("EUR").build()
        );

        List<PrizeDto> updated = challengeService.updatePrizes(created.getId(), prizes);
        assertEquals(3, updated.size());

        List<PrizeDto> fetched = challengeService.getPrizes(created.getId());
        assertEquals(3, fetched.size());
        assertEquals(3000.0, fetched.get(0).getAmount());
    }
}
