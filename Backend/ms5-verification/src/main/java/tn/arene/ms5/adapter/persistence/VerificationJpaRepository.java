package tn.arene.ms5.adapter.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import tn.arene.ms5.domain.VerificationStatus;

public interface VerificationJpaRepository extends JpaRepository<VerificationEntity, UUID> {

	Optional<VerificationEntity> findFirstByIdempotencyKeyAndStatusIn(String idempotencyKey,
			List<VerificationStatus> statuses);

	List<VerificationEntity> findBySubmissionIdOrderByCreatedAtDesc(UUID submissionId);

	List<VerificationEntity> findByStatusOrderByCreatedAtAsc(VerificationStatus status, Pageable pageable);
}
