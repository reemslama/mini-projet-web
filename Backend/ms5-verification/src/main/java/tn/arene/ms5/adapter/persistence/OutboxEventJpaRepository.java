package tn.arene.ms5.adapter.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {
	List<OutboxEventEntity> findByStatusInAndAvailableAtBefore(List<String> statuses, LocalDateTime before,
			Pageable pageable);
}
