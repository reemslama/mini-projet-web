package com.enlice.submissions.events;

import com.enlice.submissions.domain.Charge;
import com.enlice.submissions.domain.Submission;
import com.enlice.submissions.domain.SubmissionRepository;
import com.enlice.submissions.domain.SubmissionStatus;
import com.enlice.submissions.domain.Verdict;
import com.enlice.submissions.domain.VerdictRepository;
import com.enlice.submissions.domain.ChallengeWindow;
import com.enlice.submissions.domain.ChallengeWindowRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SubmissionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SubmissionEventConsumer.class);

    private final SubmissionRepository submissionRepository;
    private final VerdictRepository verdictRepository;
    private final ChallengeWindowRepository challengeWindowRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    public SubmissionEventConsumer(SubmissionRepository submissionRepository,
                                   VerdictRepository verdictRepository,
                                   ChallengeWindowRepository challengeWindowRepository,
                                   ProcessedEventRepository processedEventRepository,
                                   ObjectMapper objectMapper) {
        this.submissionRepository = submissionRepository;
        this.verdictRepository = verdictRepository;
        this.challengeWindowRepository = challengeWindowRepository;
        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void onSubmissionSubmitted(EventEnvelope envelope) {
        if (alreadyProcessed(envelope)) {
            return;
        }
        IncomingPayloads.SubmissionSubmitted payload = convert(envelope, IncomingPayloads.SubmissionSubmitted.class);
        Submission submission = submissionRepository.findById(payload.submissionId()).orElse(null);
        if (submission != null) {
            submission.setStatus(SubmissionStatus.SUBMITTED);
            submission.setCommitSha(payload.commitSha());
            submission.setSubmittedAt(Instant.now());
            submissionRepository.save(submission);
            log.info("Soumission {} soumise (sha={})", payload.submissionId(), payload.commitSha());
        }
        markProcessed(envelope);
    }

    @Transactional
    public void onVerificationCompleted(EventEnvelope envelope) {
        if (alreadyProcessed(envelope)) {
            return;
        }
        IncomingPayloads.VerificationCompleted payload = convert(envelope, IncomingPayloads.VerificationCompleted.class);
        Submission submission = submissionRepository.findById(payload.submissionId()).orElse(null);
        if (submission != null) {
            submission.setStatus("PASS".equals(payload.result()) ? SubmissionStatus.PASSED : SubmissionStatus.REJECTED);
            if ("PASS".equals(payload.result())) {
                submission.setCommitSha(payload.commitSha());
            }
            submissionRepository.save(submission);
            verdictRepository.save(new Verdict(payload.submissionId(), payload.result(), payload.passed(),
                    payload.failed(), payload.total(), payload.coverage(), null, Instant.now()));
            log.info("Verdict enregistré : {} -> {}", payload.submissionId(), payload.result());
        }
        markProcessed(envelope);
    }

    @Transactional
    public void onSubmissionRejected(EventEnvelope envelope) {
        if (alreadyProcessed(envelope)) {
            return;
        }
        IncomingPayloads.SubmissionRejected payload = convert(envelope, IncomingPayloads.SubmissionRejected.class);
        Submission submission = submissionRepository.findById(payload.submissionId()).orElse(null);
        if (submission != null) {
            submission.setStatus(SubmissionStatus.REJECTED);
            submissionRepository.save(submission);
        }
        markProcessed(envelope);
    }

    @Transactional
    public void onSimilarityFlagged(EventEnvelope envelope) {
        if (alreadyProcessed(envelope)) {
            return;
        }
        IncomingPayloads.SimilarityFlagged payload = convert(envelope, IncomingPayloads.SimilarityFlagged.class);
        Submission submission = submissionRepository.findById(payload.submissionId()).orElse(null);
        if (submission != null) {
            submission.setSimilarityFlagged(true);
            submissionRepository.save(submission);
        }
        markProcessed(envelope);
    }

    @Transactional
    public void onChallengePublished(EventEnvelope envelope) {
        if (alreadyProcessed(envelope)) {
            return;
        }
        IncomingPayloads.ChallengePublished payload = convert(envelope, IncomingPayloads.ChallengePublished.class);
        if (payload.challengeId() != null) {
            challengeWindowRepository.findById(payload.challengeId())
                    .orElseGet(() -> new ChallengeWindow(payload.challengeId(), payload.deadline()));
            ChallengeWindow window = challengeWindowRepository.findById(payload.challengeId()).orElseThrow(
                    () -> new IllegalStateException("Fenêtre de défi introuvable : " + payload.challengeId()));
            window.setDeadline(payload.deadline());
            challengeWindowRepository.save(window);
        }
        markProcessed(envelope);
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
