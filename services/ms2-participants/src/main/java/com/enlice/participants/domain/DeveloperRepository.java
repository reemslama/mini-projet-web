package com.enlice.participants.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeveloperRepository extends JpaRepository<Developer, UUID> {
    Optional<Developer> findByKeycloakId(String keycloakId);
}
