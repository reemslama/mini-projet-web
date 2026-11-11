package com.enlice.submissions.service;

import com.enlice.submissions.domain.ChargeWindowPlaceholder;
import com.enlice.submissions.domain.ChallengeWindow;
import com.enlice.submissions.domain.ChallengeWindowRepository;
import com.enlice.submissions.domain.Submission;
import com.enlice.submissions.domain.SubmissionRepository;
import com.enlice.submissions.domain.SubmissionStatus;
import com.enlice.submissions.domain.Verdict;
import com.enlice.submissions.domain.VerdictRepository;
import com.enlice.submissions.dto.SubmissionCreate;
import com.enlice.submissions.dto.SubmissionResponse;
import com.enlice.submissions.dto.VerdictResponse;
import com.enlice.submissions.events.EventPublisher;
import com.enlice.submissions.events.IncomingPayloads;
import com.enlice.submissions.web.ConflictException;
import com.enlice.submissions.web.NotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionService {

    private static final String SUBMISSION_SUBMITTED_BINDING = "submissionSubmitted-out-0";

    private final SubmissionRepository submissionRepository;
    private final VerdictRepository verdictRepository;
    private final ChallengeWindowRepository challengeWindowRepository;
    private final EventPublisher eventPublisher;

    public SubmissionService(SubmissionRepository submissionRepository,
                              VerdictRepository verdictRepository,
                              ChallengeWindowRepository challengeWindowRepository,
                              EventPublisher eventPublisher) {
        this.submissionRepository = submissionRepository;
        this.verdictRepository = verdictRepository;
        this.challengeWindowRepository = challengeWindowRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SubmissionResponse create(UUID applicantId, SubmissionCreate request, Instant now) {
        Submission submission = new Submission(request.challengeId(), applicantId, request.repoUrl(),
                request.branch(), SubmissionStatus.DRAFT, null, null, false, now);
        return SubmissionResponse.from(submissionRepository.save(submission));
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> submissionsOf(UUID challengeId, SubmissionStatus status) {
        List<Submission> submissions = status == null
                ? submissionRepository.findByChallengeId(challengeId)
                : submissionRepository.findAll().stream()
                        .filter(submission -> submission.getChallengeId().equals(challengeId)
                                && submission.getStatus() == status)
                        .toList();
        return submissions.stream().map(SubmissionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SubmissionResponse get(UUID id) {
        return SubmissionResponse.from(submissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Soumission introuvable : " + id)));
    }

    @Transactional
    public SubmissionResponse submit(UUID id, Instant now) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Soumission introuvable : " + id));
        if (submission.getStatus() != SubmissionStatus.DRAFT) {
            throw new ConflictException("La soumission est déjà fermée : statut " + submission.getStatus());
        }
        ChallengeWindow window = challengeWindowRepository.findById(submission.getChallengeId()).orElse(null);
        if (window != null && window.getDeadline() != null && window.getDeadline().isBefore(now)) {
            throw new ConflictException("Date limite dépassée : la soumission ne peut plus être envoyée");
        }
        String commitSha = new GitCommitClient().resolve(submission.getRepoUrl(), submission.getBranch());
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setCommitSha(commitSha);
        submission.setSubmittedAt(now);
        Submission saved = submissionRepository.save(submission);
        eventPublisher.publish(SUBMISSION_SUBMITTED_BINDING, "submission.submitted",
                new IncomingPayloads.SubmissionSubmitted(saved.getId(), saved.getChallengeId(), saved.getApplicantId(),
                        saved.getRepoUrl(), saved.getCommitSha()));
        return SubmissionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public VerdictResponse verdict(UUID id) {
        Verdict verdict = verdictRepository.findBySubmissionId(id)
                .orElseThrow(() -> new NotFoundException("Aucun verdict pour cette soumission"));
        return VerdictResponse.from(verdict);
    }

    @Transactional
    public VerdictResponse recordVerdict(UUID submissionId, Verdict verdict) {
        Verdict saved = verdictRepository.save(verdict);
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Soumission introuvable : " + submissionId));
        submission.setStatus("PASS".equals(verdict.getResult()) ? SubmissionStatus.PASSED : SubmissionStatus.REJECTED);
        submissionRepository.save(submission);
        return VerdictResponse.from(saved);
    }

    @Transactional
    public void flagSimilarity(UUID submissionId, UUID otherSubmissionId, double maxSimilarity) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Soumission introuvable : " + submissionId));
        submission.setSimilarityFlagged(true);
        submissionRepository.save(submission);
    }
}
