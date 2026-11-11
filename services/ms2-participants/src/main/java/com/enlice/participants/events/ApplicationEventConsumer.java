package com.enlice.participants.events;

import com.enlice.participants.domain.Application;
import com.enlice.participants.domain.ApplicationRepository;
import com.enlice.participants.domain.ApplicationStatus;
import com.enlice.participants.domain.ChallengeProjection;
import com.enlice.participants.domain.ChallengeProjectionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consommateurs d'événements du flux Métier En Lice pour MS2 (Participants).
 * Chaque traitement est idempotent (journal {@code processed_events}).
 */
@Component
public class ApplicationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ApplicationEventConsumer.class);

    private final ChallengeProjectionRepository projectionRepository;
    private final ApplicationRepository applicationRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    public ApplicationEventConsumer(ChallengeProjectionRepository projectionRepository,
                                    ApplicationRepository applicationRepository,
                                    ProcessedEventRepository processedEventRepository,
                                    ObjectMapper objectMapper) {
        this.projectionRepository = projectionRepository;
        this.applicationRepository = applicationRepository;
        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void onChallengePublished(EventEnvelope envelope) {
        if (alreadyProcessed(envelope)) {
            return;
        }
        IncomingPayloads.ChallengePublished payload = convert(envelope, IncomingPayloads.ChallengePublished.class);
        if (payload.challengeId() == null) {
            log.warn("challenge.published sans challengeId (eventId={})", envelope.eventId());
        } else {
            ChallengeProjection projection = projectionRepository.findById(payload.challengeId())
                    .orElseGet(() -> new ChallengeProjection(payload.challengeId(), payload.deadline(), payload.status(),
                            payload.publishedAt(), 0));
            projection.setDeadline(payload.deadline());
            projection.setStatus(payload.status());
            projection.setPublishedAt(payload.publishedAt());
            projection.setApplicationCount(payload.applicationCount());
            projectionRepository.save(projection);
            log.info("Projection de défi mise à jour : {}", payload.challengeId());
        }
        markProcessed(envelope);
    }

    @Transactional
    public void onApplicationRegistered(EventEnvelope envelope) {
        if (alreadyProcessed(envelope)) {
            return;
        }
        IncomingPayloads.ApplicationRegistered payload = convert(envelope, IncomingPayloads.ApplicationRegistered.class);
        if (payload.challengeId() != null) {
            projectionRepository.findById(payload.challengeId()).ifPresentOrElse(projection -> {
                projection.setApplicationCount(projection.getApplicationCount() + 1);
                projectionRepository.save(projection);
            }, () -> log.warn("application.registered pour un défi inconnu : {}", payload.challengeId()));
        }
        markProcessed(envelope);
    }

    @Transactional
    public void onVerificationCompleted(EventEnvelope envelope) {
        if (alreadyProcessed(envelope)) {
            return;
        }
        IncomingPayloads.VerificationCompleted payload = convert(envelope, IncomingPayloads.VerificationCompleted.class);
        if (payload.challengeId() != null && payload.applicantId() != null) {
            List<Application> candidates = applicationRepository
                    .findByChallengeIdAndApplicantTypeAndApplicantId(payload.challengeId(),
                            com.enlice.participants.domain.ApplicantType.DEVELOPER, payload.applicantId());
            for (Application application : candidates) {
                application.setStatus("PASS".equals(payload.result())
                        ? ApplicationStatus.ACCEPTED : ApplicationStatus.REJECTED);
            }
            applicationRepository.saveAll(candidates);
            log.info("Candidature mise à jour après vérification : {}", payload.applicantId());
        }
        markProcessed(envelope);
    }

    @Transactional
    public void onSubmissionRejected(EventEnvelope envelope) {
        if (alreadyProcessed(envelope)) {
            return;
        }
        IncomingPayloads.SubmissionRejected payload = convert(envelope, IncomingPayloads.SubmissionRejected.class);
        log.info("Soumission rejetée : {}", payload.submissionId());
        markProcessed(envelope);
    }

    @Transactional
    public void onJudgingCompleted(EventEnvelope envelope) {
        if (alreadyProcessed(envelope)) {
            return;
        }
        markProcessed(envelope);
        log.info("judging.completed reçu (eventId={})", envelope.eventId());
    }

    @Transactional
    public void onWinnersDeclared(EventEnvelope envelope) {
        if (alreadyProcessed(envelope)) {
            return;
        }
        markProcessed(envelope);
        log.info("winners.declared reçu (eventId={})", envelope.eventId());
    }

    private boolean alreadyProcessed(EventEnvelope envelope) {
        return envelope.eventId() != null && processedEventRepository.existsById(envelope.eventId().toString());
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
