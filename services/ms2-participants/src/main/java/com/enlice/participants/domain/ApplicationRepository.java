package com.enlice.participants.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    List<Application> findByChallengeId(UUID challengeId);

    List<Application> findByChallengeIdAndApplicantTypeAndApplicantId(UUID challengeId, ApplicantType applicantType,
                                                                     UUID applicantId);
}
