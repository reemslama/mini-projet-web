package com.enlice.submissions.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    java.util.List<Submission> findByChallengeId(UUID challengeId);
}
