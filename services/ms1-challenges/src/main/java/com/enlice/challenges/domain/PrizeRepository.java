package com.enlice.challenges.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrizeRepository extends JpaRepository<Prize, UUID> {

    List<Prize> findByChallengeIdOrderByRankAsc(UUID challengeId);

    long countByChallengeId(UUID challengeId);

    void deleteByChallengeId(UUID challengeId);
}
