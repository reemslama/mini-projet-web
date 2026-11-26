package tn.arene.ms5.adapter.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SimilarityReportJpaRepository extends JpaRepository<SimilarityReportEntity, UUID> {
	Optional<SimilarityReportEntity> findFirstBySubmissionIdOrderByAnalyzedAtDesc(UUID submissionId);
}
