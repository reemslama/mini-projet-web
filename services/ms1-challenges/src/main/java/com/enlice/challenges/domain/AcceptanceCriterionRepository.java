package com.enlice.challenges.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcceptanceCriterionRepository extends JpaRepository<AcceptanceCriterion, String> {

    List<AcceptanceCriterion> findByChallengeIdOrderByWeightDesc(UUID challengeId);

    long countByChallengeId(UUID challengeId);

    void deleteByChallengeId(UUID challengeId);
}
