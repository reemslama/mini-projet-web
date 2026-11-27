package tn.arene.ms1challenges.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.arene.ms1challenges.entities.Challenge;
import tn.arene.ms1challenges.entities.ChallengeStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeResponse {

    private UUID id;
    private String title;
    private String description;
    private UUID enterpriseId;
    private LocalDateTime deadline;
    private List<String> tags;
    private ChallengeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;

    public static ChallengeResponse fromEntity(Challenge challenge) {
        if (challenge == null) return null;
        return ChallengeResponse.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .enterpriseId(challenge.getEnterpriseId())
                .deadline(challenge.getDeadline())
                .tags(challenge.getTags())
                .status(challenge.getStatus())
                .createdAt(challenge.getCreatedAt())
                .publishedAt(challenge.getPublishedAt())
                .build();
    }
}
