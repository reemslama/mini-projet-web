package tn.arene.ms5.adapter.messaging;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tn.arene.ms5.adapter.persistence.OutboxEventEntity;
import tn.arene.ms5.adapter.persistence.OutboxEventJpaRepository;

/**
 * Publisher d'outbox. Le transport RabbitMQ est branché en S7 ; ici les événements sont
 * marqués PUBLISHED après log afin de garder l'ordre « DB puis broker » et l'idempotence.
 */
@Component
public class OutboxDispatcher {

	private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);

	private final OutboxEventJpaRepository repository;
	private final int batch;

	public OutboxDispatcher(OutboxEventJpaRepository repository,
			@Value("${ms5.outbox.batch:100}") int batch) {
		this.repository = repository;
		this.batch = batch;
	}

	@Scheduled(fixedDelayString = "${ms5.outbox.poll-ms:500}")
	@Transactional
	public void dispatch() {
		List<OutboxEventEntity> pending = repository.findByStatusInAndAvailableAtBefore(
				List.of("NEW", "FAILED"), LocalDateTime.now(), PageRequest.of(0, batch));
		for (OutboxEventEntity event : pending) {
			log.info("publish {} → {} ({})", event.getEventType(), event.getRoutingKey(), event.getId());
			event.setStatus("PUBLISHED");
			event.setAttempts(event.getAttempts() + 1);
			event.setPublishedAt(LocalDateTime.now());
		}
	}
}
