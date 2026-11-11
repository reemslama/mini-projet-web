package com.enlice.participants.service;

import com.enlice.participants.domain.Application;
import com.enlice.participants.domain.ApplicationRepository;
import com.enlice.participants.domain.ApplicationStatus;
import com.enlice.participants.domain.ApplicantType;
import com.enlice.participants.domain.ChallengeProjection;
import com.enlice.participants.domain.ChallengeProjectionRepository;
import com.enlice.participants.dto.ApplicationCreate;
import com.enlice.participants.dto.ApplicationResponse;
import com.enlice.participants.events.EventPublisher;
import com.enlice.participants.events.IncomingPayloads;
import com.enlice.participants.web.ConflictException;
import com.enlice.participants.web.NotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {

    private static final String APPLICATION_REGISTERED_BINDING = "applicationRegistered-out-0";

    private final ApplicationRepository applicationRepository;
    private final ChallengeProjectionRepository projectionRepository;
    private final EventPublisher eventPublisher;

    public ApplicationService(ApplicationRepository applicationRepository,
                              ChallengeProjectionRepository projectionRepository,
                              EventPublisher eventPublisher) {
        this.applicationRepository = applicationRepository;
        this.projectionRepository = projectionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ApplicationResponse apply(UUID challengeId, ApplicationCreate request, Instant now) {
        ChallengeProjection projection = projectionRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException("Défi introuvable : " + challengeId));
        if ("CLOSED".equals(projection.getStatus())) {
            throw new ConflictException("Défi clos : aucune candidature n'est plus acceptée");
        }
        if (projection.getDeadline() != null && projection.getDeadline().isBefore(now)) {
            throw new ConflictException("Défi clos : la date limite est passée");
        }
        boolean duplicate = !applicationRepository
                .findByChallengeIdAndApplicantTypeAndApplicantId(challengeId, request.applicantType(),
                        request.applicantId())
                .isEmpty();
        if (duplicate) {
            throw new ConflictException("Déjà inscrit à ce défi");
        }
        Application application = new Application(challengeId, request.applicantType(), request.applicantId(),
                ApplicationStatus.PENDING, now);
        Application saved = applicationRepository.save(application);
        eventPublisher.publish(APPLICATION_REGISTERED_BINDING, "application.registered",
                new IncomingPayloads.ApplicationRegistered(challengeId, request.applicantType(), request.applicantId()));
        return ApplicationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> applicationsOf(UUID challengeId) {
        return applicationRepository.findByChallengeId(challengeId).stream()
                .map(ApplicationResponse::from)
                .toList();
    }
}
