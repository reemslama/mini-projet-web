package com.enlice.evaluation.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertRepository extends JpaRepository<Expert, UUID> {
    Optional<Expert> findByKeycloakId(String keycloakId);
}
