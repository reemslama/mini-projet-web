package com.enlice.participants.dto;

import com.enlice.participants.domain.Application;
import com.enlice.participants.domain.ApplicantType;
import com.enlice.participants.domain.ApplicationStatus;
import java.time.Instant;
import java.util.UUID;

public record ApplicationResponse(
        UUID id,
        UUID challengeId,
        ApplicantType applicantType,
        UUID applicantId,
        ApplicationStatus status,
        Instant registeredAt) {

    public static ApplicationResponse from(Application application) {
        return new ApplicationResponse(application.getId(), application.getChallengeId(),
                application.getApplicantType(), application.getApplicantId(), application.getStatus(),
                application.getRegisteredAt());
    }
}
