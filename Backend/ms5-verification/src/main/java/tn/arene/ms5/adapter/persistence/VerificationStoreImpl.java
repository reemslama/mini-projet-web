package tn.arene.ms5.adapter.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import tn.arene.ms5.application.port.VerificationStore;
import tn.arene.ms5.domain.Verification;
import tn.arene.ms5.domain.VerificationStatus;

@Repository
@Transactional
public class VerificationStoreImpl implements VerificationStore {

	private final VerificationJpaRepository repository;

	public VerificationStoreImpl(VerificationJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Verification save(Verification verification) {
		return repository.save(VerificationEntity.from(verification)).toDomain();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Verification> findById(UUID id) {
		return repository.findById(id).map(VerificationEntity::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Verification> findActiveByIdempotencyKey(String key) {
		return repository.findFirstByIdempotencyKeyAndStatusIn(key,
				List.of(VerificationStatus.QUEUED, VerificationStatus.RUNNING)).map(VerificationEntity::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Verification> findBySubmissionId(UUID submissionId) {
		return repository.findBySubmissionIdOrderByCreatedAtDesc(submissionId).stream()
				.map(VerificationEntity::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Verification> findQueued(int limit) {
		return repository.findByStatusOrderByCreatedAtAsc(VerificationStatus.QUEUED, PageRequest.of(0, limit)).stream()
				.map(VerificationEntity::toDomain).toList();
	}
}
