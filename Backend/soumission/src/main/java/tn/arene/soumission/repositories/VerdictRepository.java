package tn.arene.soumission.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tn.arene.soumission.entities.Verdict;

@Repository
public interface VerdictRepository extends JpaRepository<Verdict, UUID> {

	Optional<Verdict> findBySubmissionId(UUID submissionId);
}
