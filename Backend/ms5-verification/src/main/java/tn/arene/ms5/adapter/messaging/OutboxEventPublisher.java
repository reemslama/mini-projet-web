package tn.arene.ms5.adapter.messaging;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tn.arene.ms5.adapter.persistence.OutboxEventEntity;
import tn.arene.ms5.adapter.persistence.OutboxEventJpaRepository;
import tn.arene.ms5.application.port.EventPublisher;

/** Écriture DB d'abord ; la publication Rabbit est faite par OutboxDispatcher. */
@Component
public class OutboxEventPublisher implements EventPublisher {

	private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);

	private final OutboxEventJpaRepository repository;
	private final ObjectMapper objectMapper;

	public OutboxEventPublisher(OutboxEventJpaRepository repository, ObjectMapper objectMapper) {
		this.repository = repository;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public void publish(String type, UUID aggregationId, Map<String, Object> payload) {
		OutboxEventEntity event = new OutboxEventEntity();
		event.setId(UUID.randomUUID()); // messageId = outbox.id
		event.setEventType(type);
		event.setRoutingKey("arene." + type.replace('.', '.'));
		event.setAggregationId(aggregationId);
		try {
			event.setPayload(objectMapper.writeValueAsString(payload));
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Impossible de sérialiser l'événement " + type, exception);
		}
		event.setStatus("NEW");
		event.setAttempts(0);
		event.setAvailableAt(LocalDateTime.now());
		event.setCreatedAt(LocalDateTime.now());
		repository.save(event);
		log.info("outbox event {} ({}) enregistré", type, event.getId());
	}
}
