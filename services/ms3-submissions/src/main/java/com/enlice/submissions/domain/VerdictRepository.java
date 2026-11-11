package com.enlice.submissions.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerdictRepository extends JpaRepository<Verdict, UUID> {
    Optional<Verdict> findBySubmissionId(UUID submissionId);
}
