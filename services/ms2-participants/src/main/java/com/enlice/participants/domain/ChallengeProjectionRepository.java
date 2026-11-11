package com.enlice.participants.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeProjectionRepository extends JpaRepository<ChallengeProjection, UUID> {
}
