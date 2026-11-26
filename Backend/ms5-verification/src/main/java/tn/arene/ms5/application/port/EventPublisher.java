package tn.arene.ms5.application.port;

import java.util.Map;
import java.util.UUID;

/** Publication via outbox : DB d'abord, Rabbit ensuite, idempotence par messageId. */
public interface EventPublisher {

	void publish(String type, UUID aggregationId, Map<String, Object> payload);
}
