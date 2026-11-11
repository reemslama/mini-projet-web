package com.enlice.challenges.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

/**
 * Publication d'événements via Spring Cloud Stream ({@link StreamBridge}).
 *
 * <p>Le JSON est sérialisé explicitement (dates ISO-8601) puis envoyé avec
 * {@code contentType: application/json}. Les échecs de publication sont journalisés
 * mais ne propagent pas d'exception : l'état métier déjà persisté reste valide
 * (un outbox transactionnel est hors périmètre — voir docs/architecture.md).</p>
 */
@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);
    private static final String PRODUCER = "ms1";
    private static final int VERSION = 1;

    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;

    public EventPublisher(StreamBridge streamBridge, ObjectMapper objectMapper) {
        this.streamBridge = streamBridge;
        this.objectMapper = objectMapper;
    }

    /**
     * @param bindingName nom du binding de sortie déclaré dans application.yml (ex. {@code challengePublished-out-0})
     * @param type        type métier de l'événement (ex. {@code challenge.published})
     * @param payload     charge utile sérialisable en JSON
     */
    public void publish(String bindingName, String type, Object payload) {
        EventEnvelope envelope = new EventEnvelope(UUID.randomUUID(), type, Instant.now(), PRODUCER, VERSION,
                objectMapper.valueToTree(payload));
        try {
            byte[] body = objectMapper.writeValueAsBytes(envelope);
            Message<byte[]> message = MessageBuilder.withPayload(body)
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .build();
            boolean sent = streamBridge.send(bindingName, message);
            if (sent) {
                log.info("Événement '{}' publié (eventId={}, binding={})", type, envelope.eventId(), bindingName);
            } else {
                log.warn("Événement '{}' non transmis (binding={})", type, bindingName);
            }
        } catch (Exception ex) {
            log.error("Échec de publication de l'événement '{}' : {}", type, ex.getMessage(), ex);
        }
    }
}