package tn.arene.reputation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tn.arene.reputation.config.DataInitializer;
import tn.arene.reputation.dto.ReputationEventRequest;
import tn.arene.reputation.entities.ReputationEventType;
import tn.arene.reputation.services.IReputationService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "eureka.client.enabled=false")
class ReputationApplicationTests {

    @Autowired
    IReputationService service;

    @Test
    void seedDataGivesAmineTheTopRank() {
        assertThat(service.ranking(null, 0, 10).get(0).developerId()).isEqualTo(DataInitializer.DEV_AMINE);
        assertThat(service.findByDeveloper(DataInitializer.DEV_AMINE).score()).isEqualTo(120);
    }

    @Test
    void sameEventTwiceCountsOnce() {
        UUID dev = UUID.randomUUID();
        UUID challenge = UUID.randomUUID();
        var event = new ReputationEventRequest(ReputationEventType.SUBMISSION_PASSED, dev, challenge, null, null);
        assertThat(service.ingest(event)).isTrue();
        assertThat(service.ingest(event)).isFalse();
        assertThat(service.findByDeveloper(dev).score()).isEqualTo(20);
        assertThat(service.findBadges(dev)).extracting("code").containsExactly("FIRST_PASS");
    }
}
