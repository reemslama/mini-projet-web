package com.enlice.challenges.events;

import com.enlice.challenges.domain.Challenge;
import com.enlice.challenges.domain.ChallengeRepository;
import com.enlice.challenges.domain.ChallengeStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consommateurs d'événements de MS1.
 * Chaque traitement est idempotent (journal {@code processed_events}) et transactionnel.
 */
@Component
public class ChallengeEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ChallengeEventConsumer.class);

    private final ChallengeRepository challengeRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    public ChallengeEventConsumer(ChallengeRepository challengeRepository,
                                  ProcessedEventRepository processedEventRepository,
                                  ObjectMapper objectMapper) {
        this.challengeRepository = challengeRepository;
        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void onApplicationRegistered(EventEnvelope envelope) {
        if (alreadyProcessed(envelope)) {
            return;
        }
        IncomingPayloads.ApplicationRegistered payload = convert(envelope,
                IncomingPayloads.ApplicationRegistered.class);
        if (payload.challengeId() == null) {
            log.warn("application.registered sans challengeId (eventId={})", envelope.eventId());
        } else {
            challengeRepository.findById(payload.challengeId()).ifPresentOrElse(
                    challenge -> {
                        challenge.setApplicationCount(challenge.getApplicationCount() + 1);
                        challengeRepository.save(challenge);
                        log.info("Candidature enregistrée pour le défi {} (total={})", challenge.getId(),
                                challenge.getApplicationCount());
                    },
                    () -> log.warn("application.registered pour un défi inconnu : {}", payload.challengeId()));
        }
        markProcessed(envelope);
    }

    @Transactional
    public void onJudgingCompleted(EventEnvelope envelope) {
        if (alreadyProcessed(envelope)) {
            return;
        }
        IncomingPayloads.JudgingCompleted payload = convert(envelope, IncomingPayloads.JudgingCompleted.class);
        if (payload.challengeId() == null) {
            log.warn("judging.completed sans challengeId (eventId={})", envelope.eventId());
        } else {
            challengeRepository.findById(payload.challengeId()).ifPresentOrElse(
                    challenge -> transition(challenge, ChallengeStatus.JUDGING),
                    () -> log.warn("judging.completed pour un défi inconnu : {}", payload.challengeId()));
        }
        markProcessed(envelope);
    }

    @Transactional
    public void onWinnersDeclared(EventEnvelope envelope) {
        if (alreadyProcessed(envelope)) {
            return;
        }
        IncomingPayloads.WinnersDeclared payload = convert(envelope, IncomingPayloads.WinnersDeclared.class);
        if (payload.challengeId() == null) {
            log.warn("winners.declared sans challengeId (eventId={})", envelope.eventId());
        } else {
            challengeRepository.findById(payload.challengeId()).ifPresentOrElse(
                    challenge -> transition(challenge, ChallengeStatus.CLOSED),
                    () -> log.warn("winners.declared pour un défi inconnu : {}", payload.challengeId()));
        }
        markProcessed(envelope);
    }

    private void transition(Challenge challenge, ChallengeStatus target) {
        if (challenge.getStatus() == ChallengeStatus.CANCELLED || challenge.getStatus() == ChallengeStatus.CLOSED) {
            log.info("Transition {} ignorée : défi {} déjà terminal ({})", target, challenge.getId(),
                    challenge.getStatus());
            return;
        }
        challenge.setStatus(target);
        challengeRepository.save(challenge);
        log.info("Défi {} passé au statut {}", challenge.getId(), target);
    }

    private boolean alreadyProcessed(EventEnvelope envelope) {
        return envelope.eventId() != null
                && processedEventRepository.existsById(envelope.eventId().toString());
    }

    private void markProcessed(EventEnvelope envelope) {
        if (envelope.eventId() != null) {
            processedEventRepository.save(new ProcessedEvent(envelope.eventId().toString(), Instant.now()));
        }
    }

    private <T> T convert(EventEnvelope envelope, Class<T> type) {
        return objectMapper.convertValue(envelope.payload(), type);
    }
}
