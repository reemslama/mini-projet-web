package tn.arene.ms5.adapter.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeEmbeddingJpaRepository extends JpaRepository<CodeEmbeddingEntity, UUID> {
	List<CodeEmbeddingEntity> findByChallengeIdAndModelVersionAndSubmissionIdNot(
			UUID challengeId, String modelVersion, UUID submissionId);
}
