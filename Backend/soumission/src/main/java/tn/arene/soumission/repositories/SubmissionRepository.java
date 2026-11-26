package tn.arene.soumission.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tn.arene.soumission.entities.Submission;
import tn.arene.soumission.entities.SubmissionStatus;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

	List<Submission> findByChallengeId(UUID challengeId);

	List<Submission> findByChallengeIdAndStatus(UUID challengeId, SubmissionStatus status);

	Optional<Submission> findByChallengeIdAndApplicantId(UUID challengeId, UUID applicantId);
}
